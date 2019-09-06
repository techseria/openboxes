import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import update from 'immutability-helper';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
import apiClient, { flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import PackingSplitLineModal from './modals/PackingSplitLineModal';
import { debounceUsersFetch } from '../../utils/option-utils';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const FIELDS = {
  packPageItems: {
    type: ArrayField,
    virtualized: true,
    fields: {
      productCode: {
        type: LabelField,
        flexWidth: '0.7',
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product Name',
        flexWidth: '3',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      binLocationName: {
        type: LabelField,
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin location',
        flexWidth: '1',
      },
      lotNumber: {
        type: LabelField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1',
      },
      quantityShipped: {
        type: LabelField,
        label: 'react.stockMovement.quantityShipped.label',
        defaultMessage: 'Qty shipped',
        flexWidth: '0.8',
      },
      uom: {
        type: LabelField,
        label: 'react.stockMovement.uom.label',
        defaultMessage: 'UoM',
        flexWidth: '0.8',
      },
      recipient: {
        type: SelectField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '2.5',
        fieldKey: '',
        attributes: {
          async: true,
          required: true,
          showValueTooltip: true,
          openOnClick: false,
          autoload: false,
          cache: false,
          options: [],
          labelKey: 'name',
          filterOptions: options => options,
        },
        getDynamicAttr: props => ({
          loadOptions: props.debouncedUsersFetch,
        }),
      },
      palletName: {
        type: TextField,
        label: 'react.stockMovement.pallet.label',
        defaultMessage: 'Pallet',
        flexWidth: '0.8',
      },
      boxName: {
        type: TextField,
        label: 'react.stockMovement.box.label',
        defaultMessage: 'Box',
        flexWidth: '0.8',
      },
      splitLineItems: {
        type: PackingSplitLineModal,
        label: 'react.stockMovement.splitLine.label',
        defaultMessage: 'Split line',
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          title: 'react.stockMovement.splitLine.label',
          btnOpenText: 'react.stockMovement.splitLine.label',
          btnOpenDefaultText: 'Split line',
          btnOpenClassName: 'btn btn-outline-success',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, onSave, formValues,
        }) => ({
          lineItem: fieldValue,
          onSave: splitLineItems => onSave(formValues, rowIndex, splitLineItems),
        }),
      },
    },
  },
};

/**
 * The fifth step of stock movement(for movements from a depot) where user can see the
 * packing information.
 */
class PackingPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: { ...this.props.initialValues, packPageItems: [] },
    };

    this.saveSplitLines = this.saveSplitLines.bind(this);

    this.debouncedUsersFetch =
      debounceUsersFetch(this.props.debounceTime, this.props.minSearchLength);

    this.props.showSpinner();
  }

  componentDidMount() {
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  dataFetched = false;

  /**
   * Fetches all required data.
   * @public
   */
  fetchAllData() {
    this.fetchLineItems().then((resp) => {
      const { packPageItems } = resp.data.data.packPage;
      this.setState({ values: { ...this.state.values, packPageItems } });
      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  /**
   * Saves packing data
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    this.props.showSpinner();
    this.savePackingData(formValues.packPageItems)
      .then((resp) => {
        const { packPageItems } = resp.data.data.packPage;
        this.setState({ values: { ...this.state.values, packPageItems } });
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Refetch the data, all not saved changes will be lost.
   * @public
   */
  refresh() {
    confirmAlert({
      title: this.props.translate('react.stockMovement.message.confirmRefresh.label', 'Confirm refresh'),
      message: this.props.translate(
        'react.stockMovement.confirmRefresh.message',
        'Are you sure you want to refresh? Your progress since last save will be lost.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => this.fetchAllData(),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Transition to next stock movement status
   * @public
   */
  transitionToNextStep() {
    const url = `${process.env.REACT_APP_BASE_NAME}/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'CHECKING' };

    return apiClient.post(url, payload);
  }

  /**
   * Fetches 5th step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `${process.env.REACT_APP_BASE_NAME}/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=5`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.savePackingData(formValues.packPageItems)
      .then(() => {
        this.transitionToNextStep()
          .then(() => {
            this.props.hideSpinner();
            this.props.onSubmit(formValues);
          })
          .catch(() => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves packing data
   * @param {object} packPageItems
   * @public
   */
  savePackingData(packPageItems) {
    const updateItemsUrl = `${process.env.REACT_APP_BASE_NAME}/api/stockMovements/${this.state.values.stockMovementId}/updateShipmentItems`;
    const payload = {
      id: this.state.values.stockMovementId,
      stepNumber: '5',
      packPageItems,
    };

    if (payload.packPageItems.length) {
      return apiClient.post(updateItemsUrl, flattenRequest(payload))
        .catch(() => Promise.reject(new Error('react.stockMovement.error.saveRequisitionItems.label')));
    }

    return Promise.resolve();
  }

  /**
   * Saves split line items
   * @param {object} formValues
   * @param {number} lineItemIndex
   * @param {object} splitLineItems
   * @public
   */
  saveSplitLines(formValues, lineItemIndex, splitLineItems) {
    this.props.showSpinner();
    this.savePackingData(update(formValues.packPageItems, {
      [lineItemIndex]: {
        splitLineItems: { $set: splitLineItems },
      },
    }))
      .then((resp) => {
        const { packPageItems } = resp.data.data.packPage;
        this.setState({ values: { ...this.state.values, packPageItems } });
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <span>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-refresh pr-2" />
                  <Translate id="react.default.button.refresh.label" defaultMessage="Reload" />
                </span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
              >
                <span><i className="fa fa-save pr-2" />
                  <Translate id="react.default.button.save.label" defaultMessage="Save" />
                </span>
              </button>
              <button
                type="button"
                onClick={() => this.savePackingData(values.packPageItems).then(() => { window.location = `${process.env.REACT_APP_BASE_NAME}/stockMovement/show/${values.stockMovementId}`; })}
                className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
              >
                <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                onSave: this.saveSplitLines,
                formValues: values,
                debouncedUsersFetch: this.debouncedUsersFetch,
              }))}
              <div>
                <button type="button" className="btn btn-outline-primary btn-form btn-xs" onClick={() => this.savePackingData(values.packPageItems).then(() => this.props.previousPage(values))}>
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button type="submit" className="btn btn-outline-primary btn-form float-right btn-xs">
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  recipients: state.users.data,
  recipientsFetched: state.users.fetched,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(PackingPage));

PackingPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  onSubmit: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
