# coding: utf-8

"""
    IBM Cohort Engine

    Service to evaluate cohorts and measures  # noqa: E501

    OpenAPI spec version: 0.0.1 2021-04-26T16:43:57Z
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from __future__ import absolute_import

import re  # noqa: F401

# python 2 and python 3 compatibility library
import six

from swagger_client.api_client import ApiClient


class ValueSetApi(object):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    Ref: https://github.com/swagger-api/swagger-codegen
    """

    def __init__(self, api_client=None):
        if api_client is None:
            api_client = ApiClient()
        self.api_client = api_client

    def create_value_set(self, version, fhir_data_server_config, value_set, **kwargs):  # noqa: E501
        """Insert a new value set to the fhir server or, if it already exists, update it in place  # noqa: E501

        Uploads a value set described by the given xslx file  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_value_set(version, fhir_data_server_config, value_set, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str version: The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (required)
        :param file fhir_data_server_config: A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/getting-started.md#fhir-server-configuration for more details.  Example Contents:   <pre>{     \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",     \"endpoint\": \"https://fhir-internal.dev:9443/fhir-server/api/v4\",     \"user\": \"fhiruser\",     \"password\": \"replaceWithfhiruserPassword\",     \"logInfo\": [         \"ALL\"     ],     \"tenantId\": \"default\" }</pre> (required)
        :param file value_set: Spreadsheet containing the Value Set definition. (required)
        :param bool update_if_exists: The parameter that, if true, will force updates of value sets if the value set already exists
        :param file custom_code_system: A custom mapping of code systems to urls
        :return: None
                 If the method is called asynchronously,
                 returns the request thread.
        """
        kwargs['_return_http_data_only'] = True
        if kwargs.get('async_req'):
            return self.create_value_set_with_http_info(version, fhir_data_server_config, value_set, **kwargs)  # noqa: E501
        else:
            (data) = self.create_value_set_with_http_info(version, fhir_data_server_config, value_set, **kwargs)  # noqa: E501
            return data

    def create_value_set_with_http_info(self, version, fhir_data_server_config, value_set, **kwargs):  # noqa: E501
        """Insert a new value set to the fhir server or, if it already exists, update it in place  # noqa: E501

        Uploads a value set described by the given xslx file  # noqa: E501
        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please pass async_req=True
        >>> thread = api.create_value_set_with_http_info(version, fhir_data_server_config, value_set, async_req=True)
        >>> result = thread.get()

        :param async_req bool
        :param str version: The release date of the version of the API you want to use. Specify dates in YYYY-MM-DD format. (required)
        :param file fhir_data_server_config: A configuration file containing information needed to connect to the FHIR server. See https://github.com/Alvearie/quality-measure-and-cohort-service/blob/main/docs/user-guide/getting-started.md#fhir-server-configuration for more details.  Example Contents:   <pre>{     \"@class\": \"com.ibm.cohort.fhir.client.config.IBMFhirServerConfig\",     \"endpoint\": \"https://fhir-internal.dev:9443/fhir-server/api/v4\",     \"user\": \"fhiruser\",     \"password\": \"replaceWithfhiruserPassword\",     \"logInfo\": [         \"ALL\"     ],     \"tenantId\": \"default\" }</pre> (required)
        :param file value_set: Spreadsheet containing the Value Set definition. (required)
        :param bool update_if_exists: The parameter that, if true, will force updates of value sets if the value set already exists
        :param file custom_code_system: A custom mapping of code systems to urls
        :return: None
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['version', 'fhir_data_server_config', 'value_set', 'update_if_exists', 'custom_code_system']  # noqa: E501
        all_params.append('async_req')
        all_params.append('_return_http_data_only')
        all_params.append('_preload_content')
        all_params.append('_request_timeout')

        params = locals()
        for key, val in six.iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method create_value_set" % key
                )
            params[key] = val
        del params['kwargs']
        # verify the required parameter 'version' is set
        if self.api_client.client_side_validation and ('version' not in params or
                                                       params['version'] is None):  # noqa: E501
            raise ValueError("Missing the required parameter `version` when calling `create_value_set`")  # noqa: E501
        # verify the required parameter 'fhir_data_server_config' is set
        if self.api_client.client_side_validation and ('fhir_data_server_config' not in params or
                                                       params['fhir_data_server_config'] is None):  # noqa: E501
            raise ValueError("Missing the required parameter `fhir_data_server_config` when calling `create_value_set`")  # noqa: E501
        # verify the required parameter 'value_set' is set
        if self.api_client.client_side_validation and ('value_set' not in params or
                                                       params['value_set'] is None):  # noqa: E501
            raise ValueError("Missing the required parameter `value_set` when calling `create_value_set`")  # noqa: E501

        collection_formats = {}

        path_params = {}

        query_params = []
        if 'version' in params:
            query_params.append(('version', params['version']))  # noqa: E501
        if 'update_if_exists' in params:
            query_params.append(('update_if_exists', params['update_if_exists']))  # noqa: E501

        header_params = {}

        form_params = []
        local_var_files = {}
        if 'fhir_data_server_config' in params:
            local_var_files['fhir_data_server_config'] = params['fhir_data_server_config']  # noqa: E501
        if 'value_set' in params:
            local_var_files['value_set'] = params['value_set']  # noqa: E501
        if 'custom_code_system' in params:
            local_var_files['custom_code_system'] = params['custom_code_system']  # noqa: E501

        body_params = None
        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.select_header_accept(
            ['application/json'])  # noqa: E501

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.select_header_content_type(  # noqa: E501
            ['multipart/form-data'])  # noqa: E501

        # Authentication setting
        auth_settings = []  # noqa: E501

        return self.api_client.call_api(
            '/v1/valueset', 'POST',
            path_params,
            query_params,
            header_params,
            body=body_params,
            post_params=form_params,
            files=local_var_files,
            response_type=None,  # noqa: E501
            auth_settings=auth_settings,
            async_req=params.get('async_req'),
            _return_http_data_only=params.get('_return_http_data_only'),
            _preload_content=params.get('_preload_content', True),
            _request_timeout=params.get('_request_timeout'),
            collection_formats=collection_formats)