import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import update from 'immutability-helper';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import { renderFormField } from '../../utils/form-utils';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
import SubstitutionsModal from './modals/SubstitutionsModal';
import apiClient from '../../utils/apiClient';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../actions';
import ButtonField from '../form-elements/ButtonField';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const BTN_CLASS_MAPPER = {
  YES: 'btn btn-outline-success',
  NO: 'disabled btn btn-outline-secondary',
  EARLIER: 'btn btn-outline-warning',
  HIDDEN: 'btn invisible',
};

const FIELDS = {
  editPageItems: {
    type: ArrayField,
    virtualized: true,
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({ rowValues, subfield }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (!subfield) { className += 'font-weight-bold'; }
      return { className };
    },
    subfieldKey: 'substitutionItems',
    fields: {
      productCode: {
        type: LabelField,
        flexWidth: '0.6',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
      },
      productName: {
        type: LabelField,
        flexWidth: '4.5',
        label: 'react.stockMovement.productName.label',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      quantityRequested: {
        type: LabelField,
        label: 'react.stockMovement.quantityRequested.label',
        defaultMessage: 'Qty requested',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty available',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = '';
          if (!fieldValue.quantityAvailable ||
            fieldValue.quantityAvailable < fieldValue.quantityRequested) {
            className = 'text-danger';
          }
          return {
            className,
          };
        },
        attributes: {
          formatValue: value => (value.quantityAvailable ? (value.quantityAvailable.toLocaleString('en-US')) : value.quantityAvailable),
        },
      },
      quantityConsumed: {
        type: LabelField,
        label: 'react.stockMovement.monthlyQuantity.label',
        defaultMessage: 'Monthly stocklist qty',
        flexWidth: '1.45',
        getDynamicAttr: ({ hasStockList, translate }) => ({
          formatValue: (value) => {
            if (value && value !== '0') {
              return value.toLocaleString('en-US');
            } else if (hasStockList) {
              return translate('react.stockMovement.replenishmentPeriodNotFound.label', 'Replenishment period not found');
            }

            return '0';
          },
          showValueTooltip: true,
        }),
      },
      substituteButton: {
        label: 'react.stockMovement.substitution.label',
        defaultMessage: 'Substitution',
        type: SubstitutionsModal,
        fieldKey: '',
        flexWidth: '1',
        attributes: {
          title: 'react.stockMovement.substitutes.label',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId, onResponse,
          reviseRequisitionItems, values, reasonCodes,
        }) => ({
          onOpen: () => reviseRequisitionItems(values),
          productCode: fieldValue.productCode,
          btnOpenText: `react.stockMovement.${fieldValue.substitutionStatus}.label`,
          btnOpenDefaultText: `${fieldValue.substitutionStatus}`,
          btnOpenDisabled: fieldValue.substitutionStatus === 'NO' || fieldValue.statusCode === 'SUBSTITUTED',
          btnOpenClassName: BTN_CLASS_MAPPER[fieldValue.substitutionStatus || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
          reasonCodes,
        }),
      },
      quantityRevised: {
        label: 'react.stockMovement.quantityRevised.label',
        defaultMessage: 'Qty revised',
        type: TextField,
        fieldKey: 'statusCode',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ fieldValue, subfield }) => ({
          disabled: fieldValue === 'SUBSTITUTED' || subfield,
        }),
      },
      reasonCode: {
        type: SelectField,
        label: 'react.stockMovement.reasonCode.label',
        defaultMessage: 'Reason code',
        flexWidth: '1.4',
        fieldKey: 'quantityRevised',
        getDynamicAttr: ({ fieldValue, subfield, reasonCodes }) => ({
          disabled: !fieldValue || subfield,
          options: reasonCodes,
          showValueTooltip: true,
        }),
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undo.label',
        defaultMessage: 'Undo',
        flexWidth: '0.9',
        fieldKey: '',
        buttonLabel: 'react.default.button.undo.label',
        buttonDefaultMessage: 'Undo',
        getDynamicAttr: ({ fieldValue, revertItem }) => ({
          onClick: fieldValue.requisitionItemId ?
            () => revertItem(fieldValue.requisitionItemId) : () => null,
          hidden: fieldValue.statusCode ? !_.includes(['CHANGED', 'CANCELED'], fieldValue.statusCode) : false,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

function validateForSave(values) {
  const errors = {};
  errors.editPageItems = [];

  _.forEach(values.editPageItems, (item, key) => {
    if (!_.isEmpty(item.quantityRevised) && _.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { reasonCode: 'react.stockMovement.errors.reasonCodeRequired.label' };
    } else if (_.isNil(item.quantityRevised) && !_.isEmpty(item.reasonCode) && item.statusCode !== 'SUBSTITUTED') {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.revisedQuantityRequired.label' };
    }
    if (parseInt(item.quantityRevised, 10) === item.quantityRequested) {
      errors.editPageItems[key] = {
        quantityRevised: 'react.stockMovement.errors.sameRevisedQty.label',
      };
    }
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised > item.quantityAvailable)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.higherQty.label' };
    }
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised < 0)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.negativeQty.label' };
    }
  });
  return errors;
}

function validate(values) {
  const errors = validateForSave(values);

  _.forEach(values.editPageItems, (item, key) => {
    if (_.isNil(item.quantityRevised) && (item.quantityRequested > item.quantityAvailable) && (item.statusCode !== 'SUBSTITUTED')) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.lowerQty.label' };
    }
  });
  return errors;
}

/**
 * The third step of stock movement(for movements from a depot) where user can see the
 * stock available and adjust quantities or make substitutions based on that information.
 */
class EditItemsPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      statusCode: '',
      revisedItems: [],
      values: { ...this.props.initialValues, editPageItems: [] },
    };

    this.revertItem = this.revertItem.bind(this);
    this.fetchEditPageItems = this.fetchEditPageItems.bind(this);
    this.reviseRequisitionItems = this.reviseRequisitionItems.bind(this);
    this.props.showSpinner();
  }

  componentDidMount() {
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchAllData(false);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchAllData(false);
    }
  }

  dataFetched = false;

  /**
   * Fetches all required data.
   * @param {boolean} forceFetch
   * @public
   */
  fetchAllData(forceFetch) {
    if (!this.props.reasonCodesFetched || forceFetch) {
      this.fetchData(this.props.fetchReasonCodes);
    }

    this.props.showSpinner();
    this.fetchLineItems().then((resp) => {
      const { statusCode, editPage } = resp.data.data;
      const editPageItems = _.map(
        editPage.editPageItems,
        val => ({
          ...val,
          disabled: true,
          quantityAvailable: val.quantityAvailable > 0 ? val.quantityAvailable : 0,
          product: {
            ...val.product,
            label: `${val.productCode} ${val.productName}`,
          },
          substitutionItems: _.map(val.substitutionItems, sub => ({
            ...sub,
            requisitionItemId: val.requisitionItemId,
          })),
        }),
      );

      this.setState({
        statusCode,
        revisedItems: _.filter(editPageItems, item => item.statusCode === 'CHANGED'),
        values: { ...this.state.values, editPageItems },
      });

      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  /**
   * Fetches data using function given as an argument(reducers components).
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sends data of revised items with post method.
   * @param {object} values
   * @public
   */
  reviseRequisitionItems(values) {
    const itemsToRevise = _.filter(
      values.editPageItems,
      (item) => {
        if (item.quantityRevised && item.reasonCode) {
          const oldRevision = _.find(
            this.state.revisedItems,
            revision => revision.requisitionItemId === item.requisitionItemId,
          );
          return _.isEmpty(oldRevision) ? true :
            ((_.toInteger(oldRevision.quantityRevised) !== _.toInteger(item.quantityRevised)) ||
              (oldRevision.reasonCode !== item.reasonCode));
        }
        return false;
      },
    );
    const url = `${process.env.REACT_APP_BASE_NAME}/api/stockMovements/${this.state.values.stockMovementId}/reviseItems`;
    const payload = {
      lineItems: _.map(itemsToRevise, item => ({
        id: item.requisitionItemId,
        quantityRevised: item.quantityRevised,
        reasonCode: item.reasonCode,
      })),
    };

    if (payload.lineItems.length) {
      return apiClient.post(url, payload);
    }

    return Promise.resolve();
  }

  /**
   * Saves list of requisition items in current step (without step change).
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    this.props.showSpinner();

    const errors = validateForSave(formValues).editPageItems;

    if (errors.length) {
      let errorMessage = `${this.props.translate('react.stockMovement.errors.errorInLine.label', 'Error occurred in line')}:</br>`;
      errorMessage += _.reduce(
        errors,
        (message, value, key) => (
          `${message}${value ? `${key + 1} - ${_.map(value, val => this.props.translate(`${val}`))}</br>` : ''}`
        ),
        '',
      );

      Alert.error(errorMessage);

      this.props.hideSpinner();
      return null;
    }

    return this.reviseRequisitionItems(formValues)
      .then((resp) => {
        const editPageItems = _.get(resp, 'data.data.editPage.editPageItems');
        if (editPageItems && editPageItems.length) {
          this.setState({
            revisedItems: _.filter(editPageItems, item => item.statusCode === 'CHANGED'),
          });
        }
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
          onClick: () => {
            this.setState({
              revisedItems: [],
            });
            this.fetchAllData(true);
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Transition to next stock movement status (PICKING)
   * after sending createPicklist: 'true' to backend autopick functionality is invoked.
   * @public
   */
  transitionToNextStep() {
    const url = `${process.env.REACT_APP_BASE_NAME}/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = {
      status: 'PICKING',
      createPicklist: this.state.statusCode === 'VERIFYING' ? 'true' : 'false',
    };

    return apiClient.post(url, payload);
  }

  /**
   * Fetches 3rd step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `${process.env.REACT_APP_BASE_NAME}/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=3`;

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
    this.reviseRequisitionItems(formValues)
      .then(() => {
        this.transitionToNextStep()
          .then(() => this.props.onSubmit(formValues))
          .catch(() => this.props.hideSpinner());
      }).catch(() => this.props.hideSpinner());
  }

  /**
   * Saves changes made when item reverted.
   * @param {object} editPageItem
   * @public
   */
  updateEditPageItem(editPageItem) {
    const editPageItemIndex = _.findIndex(this.state.values.editPageItems, item =>
      item.requisitionItemId === editPageItem.requisitionItemId);

    this.setState({
      values: {
        ...this.state.values,
        editPageItems: update(this.state.values.editPageItems, {
          [editPageItemIndex]: {
            $set: {
              ...editPageItem,
              quantityAvailable: editPageItem.quantityAvailable || 0,
              substitutionItems: _.map(editPageItem.substitutionItems, sub => ({
                ...sub,
                requisitionItemId: editPageItem.requisitionItemId,
              })),
            },
          },
        }),
      },
    });
  }

  /**
   * Saves changes made in subsitution modal and updates data.
   * @public
   */
  fetchEditPageItems() {
    this.fetchLineItems().then((resp) => {
      const { editPage } = resp.data.data;

      this.setState({
        values: {
          ...this.state.values,
          editPageItems: _.map(editPage.editPageItems, item => ({
            ...item,
            quantityAvailable: item.quantityAvailable || 0,
            substitutionItems: _.map(item.substitutionItems, sub => ({
              ...sub,
              requisitionItemId: item.requisitionItemId,
            })),
          })),
        },
      });

      this.props.hideSpinner();
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} formValues
   * @public
   */
  saveAndExit(formValues) {
    const errors = validateForSave(formValues).editPageItems;

    if (errors.length) {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmExit.label', 'Confirm save'),
        message: this.props.translate(
          'react.stockMovement.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => { window.location = `${process.env.REACT_APP_BASE_NAME}/stockMovement/show/${formValues.stockMovementId}`; },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
      this.props.hideSpinner();
    } else {
      this.reviseRequisitionItems(formValues)
        .then(() => {
          window.location = `${process.env.REACT_APP_BASE_NAME}/stockMovement/show/${formValues.stockMovementId}`;
        });
    }
  }

  /**
   * Reverts to previous state of requisition item (reverts substitutions and quantity revisions)
   * @param {string} itemId
   * @public
   */
  revertItem(itemId) {
    this.props.showSpinner();
    const revertItemsUrl = `${process.env.REACT_APP_BASE_NAME}/api/stockMovementItems/${itemId}/revertItem`;

    return apiClient.post(revertItemsUrl)
      .then((response) => {
        const editPageItem = response.data.data;
        this.updateEditPageItem(editPageItem);
        this.props.hideSpinner();
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.stockMovement.error.revertRequisitionItem.label', 'Could not revert requisition items')));
      });
  }

  /**
   * Saves changes made by user in this step and go back to previous page
   * @param {object} values
   * @param {boolean} invalid
   * @public
   */
  previousPage(values, invalid) {
    if (!invalid) {
      this.reviseRequisitionItems(values)
        .then(() => this.props.previousPage(values));
    } else {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('react.stockMovement.confirmPreviousPage.message.label', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => this.props.previousPage(values),
          },
        ],
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        validate={validate}
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
                <span><i className="fa fa-refresh pr-2" /><Translate id="react.default.button.refresh.label" defaultMessage="Reload" /></span>
              </button>
              <button
                type="button"
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-save pr-2" /><Translate id="react.default.button.save.label" defaultMessage="Save" /></span>
              </button>
              <button
                type="button"
                onClick={() => this.saveAndExit(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
              >
                <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockMovementId: values.stockMovementId,
                hasStockList: !!_.get(values.stocklist, 'id'),
                translate: this.props.translate,
                reasonCodes: this.props.reasonCodes,
                onResponse: this.fetchEditPageItems,
                revertItem: this.revertItem,
                reviseRequisitionItems: this.reviseRequisitionItems,
                values,
              }))}
              <div>
                <button
                  type="submit"
                  onClick={() => this.previousPage(values, invalid)}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => {
                    if (!invalid) {
                      this.nextPage(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                >
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
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
});

export default connect(mapStateToProps, {
  fetchReasonCodes, showSpinner, hideSpinner,
})(EditItemsPage);

EditItemsPage.propTypes = {
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
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Indicator if reason codes' data is fetched */
  reasonCodesFetched: PropTypes.bool.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
};
