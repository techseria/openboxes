import _ from 'lodash';
import queryString from 'query-string';
import apiClient from './apiClient';

export const debounceUsersFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`${process.env.REACT_APP_BASE_NAME}/api/persons?name=${searchTerm}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  ...obj,
                },
                name: obj.name,
                label: obj.name,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceLocationsFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`${process.env.REACT_APP_BASE_NAME}/api/locations?direction=${queryString.parse(window.location.search).direction}&name=${searchTerm}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  type: obj.locationType.locationTypeCode,
                  name: obj.name,
                  label: `${obj.name} [${obj.locationType.description}]`,
                },
                label: `${obj.name} [${obj.locationType.description}]`,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceGlobalSearch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`${process.env.REACT_APP_BASE_NAME}/json/globalSearch?term=${searchTerm}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data, obj => (
              {
                value: {
                  url: obj.url,
                },
                label: obj.label,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceProductsFetch = (waitTime, minSearchLength, locationId) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`${process.env.REACT_APP_BASE_NAME}/api/products?name=${searchTerm}&productCode=${searchTerm}&location.id=${locationId}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  name: obj.name,
                  productCode: obj.productCode,
                  label: `${obj.productCode} - ${obj.name}`,
                },
                label: `${obj.productCode} - ${obj.name}`,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);
