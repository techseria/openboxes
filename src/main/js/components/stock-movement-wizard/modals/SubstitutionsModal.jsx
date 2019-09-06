import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import SelectField from '../../form-elements/SelectField';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import Translate from '../../../utils/Translate';

const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'react.stockMovement.reasonFor.label',
    defaultMessage: 'Reason for not fulfilling full qty',
    attributes: {
      required: true,
    },
    getDynamicAttr: props => ({
      options: props.reasonCodes,
      hidden: !props.originalItem,
    }),
  },
  substitutions: {
    type: ArrayField,
    getDynamicRowAttr: ({ rowValues, originalItem }) => {
      let className = '';
      const rowDate = new Date(rowValues.minExpirationDate);
      const origDate = originalItem && originalItem.minExpirationDate ?
        new Date(originalItem.minExpirationDate) : null;
      if (!rowValues.originalItem) {
        className = (origDate && rowDate && rowDate < origDate) || (!origDate && rowDate) ? 'text-danger' : '';
      } else {
        className = 'font-weight-bold';
      }
      return { className };
    },
    fields: {
      productCode: {
        type: LabelField,
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
      },
      minExpirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty Available',
        fixedWidth: '150px',
        fieldKey: '',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'quantityAvailable') ? _.get(fieldValue, 'quantityAvailable').toLocaleString('en-US') : null),
          showValueTooltip: true,
        },
        getDynamicAttr: ({ fieldValue }) => ({
          tooltipValue: _.map(fieldValue.availableItems, availableItem =>
            (
              <p>{fieldValue.productCode} {fieldValue.productName}, {availableItem.expirationDate ? availableItem.expirationDate : '---'}, Qty {availableItem.quantityAvailable}</p>
            )),
        }),
      },
      quantitySelected: {
        type: TextField,
        label: 'react.stockMovement.quantitySelected.label',
        defaultMessage: 'Quantity selected',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.substitutions = [];
  let originalItem = null;
  let subQty = 0;

  _.forEach(values.substitutions, (item, key) => {
    if (item.originalItem) {
      originalItem = item;
    }
    if (item.quantitySelected) {
      subQty += _.toInteger(item.quantitySelected);
    }

    if (item.quantitySelected > item.quantityAvailable) {
      errors.substitutions[key] = { quantitySelected: 'react.stockMovement.errors.higherQtySelected.label' };
    }
    if (item.quantitySelected < 0) {
      errors.substitutions[key] = { quantitySelected: 'react.stockMovement.errors.negativeQtySelected.label' };
    }
  });

  if (originalItem && originalItem.quantitySelected && subQty < originalItem.quantityRequested
    && !values.reasonCode) {
    errors.reasonCode = 'react.default.error.requiredField.label';
  }
  return errors;
}

/**
 * Modal window where user can choose substitution and it's quantity.
 * It is available only when there is a substitution for an item.
 */
/* eslint no-param-reassign: "error" */
class SubstitutionsModal extends Component {
  constructor(props) {
    super(props);
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
      formValues: {},
      originalItem: null,
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /** Loads available substitutions for chosen item into modal's form.
   * @public
   */
  onOpen() {
    this.state.attr.onOpen();
    let substitutions = this.state.attr.lineItem.availableSubstitutions;
    let originalItem = null;

    if (_.toInteger(this.state.attr.lineItem.quantityAvailable) > 0) {
      originalItem = { ...this.state.attr.lineItem, originalItem: true };
      substitutions = [
        originalItem,
        ...this.state.attr.lineItem.availableSubstitutions,
      ];
    }

    this.setState({
      formValues: {
        substitutions,
        reasonCode: originalItem ? '' : 'SUBSTITUTION',
      },
      originalItem,
    });
  }

  /** Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const substitutions = _.filter(values.substitutions, sub =>
      sub.quantitySelected > 0 && !sub.originalItem);
    const subQty = _.reduce(values.substitutions, (sum, val) =>
      (sum + (!val.originalItem ? _.toInteger(val.quantitySelected) : 0)), 0);
    const originalItem = _.find(values.substitutions, sub => sub.originalItem)
      || this.state.attr.lineItem;

    const url = `${process.env.REACT_APP_BASE_NAME}/api/stockMovementItems/${originalItem.requisitionItemId}/substituteItem`;
    const payload = {
      newQuantity: originalItem.quantitySelected && originalItem.quantitySelected !== '0' ? originalItem.quantityRequested - subQty : '',
      quantityRevised: originalItem.quantitySelected,
      reasonCode: values.reasonCode,
      sortOrder: originalItem.sortOrder,
      substitutionItems: _.map(substitutions, sub => ({
        'newProduct.id': sub.productId,
        newQuantity: sub.quantitySelected,
        reasonCode: 'SUBSTITUTION',
        sortOrder: originalItem.sortOrder,
      })),
    };

    apiClient.post(url, payload)
      .then(() => { this.props.onResponse(); })
      .catch(() => { this.props.hideSpinner(); });
  }

  /** Sums up quantity selected from all available substitutions.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculateSelected(values) {
    return (
      <div>
        <div className="form-group pb-2">
          <label htmlFor="quantity-selected" className="col-sm-2 col-form-label font-weight-bold">
            <Translate id="react.stockMovement.quantitySelected.label" defaultMessage="Quantity selected" />
          </label>
          <div id="quantity-selected">
            {_.reduce(values.substitutions, (sum, val) =>
                (sum + (val.quantitySelected ? _.toInteger(val.quantitySelected) : 0)), 0)
            }
          </div>
        </div>
      </div>
    );
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        validate={validate}
        initialValues={this.state.formValues}
        formProps={{
          reasonCodes: this.state.attr.reasonCodes,
          originalItem: this.state.originalItem,
        }}
        renderBodyWithValues={this.calculateSelected}
      >
        <div>
          <div className="form-group">
            <label htmlFor="product" className="col-sm-2 col-form-label font-weight-bold"><Translate id="react.stockMovement.product.label" defaultMessage="Product" /></label>
            <div id="product" className="col-sm-10">{this.state.attr.lineItem.productCode} {this.state.attr.lineItem.productName}</div>
          </div>
          <div className="form-group">
            <label htmlFor="quantity-requested" className="col-sm-2 col-form-label font-weight-bold"><Translate id="react.stockMovement.quantityRequested.label" defaultMessage="Quantity requested" /></label>
            <div id="quantity-requested" className="col-sm-10">{this.state.attr.lineItem.quantityRequested}</div>
          </div>
        </div>
      </ModalWrapper>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(SubstitutionsModal);

SubstitutionsModal.propTypes = {
  /** Name of the field */
  fieldName: PropTypes.string.isRequired,
  /** Configuration of the field */
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  /** Stock movement's ID */
  stockMovementId: PropTypes.string.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
};
