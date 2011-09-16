demo = window.demo || {};


demo.ajax = demo.ajax || (function($) {
    
    //
    //private members
    //
    
    /**
     * 
     * debug errors
     * 
     */
    var errorTrap = function(userErrorHandler){
        
        if(!userErrorHandler){
            userErrorHandler = function(XMLHttpRequest, textStatus, errorThrown){
                if (errorThrown) {
                    alert("Error trap added by demo.ajax. Status [" + XMLHttpRequest.status + "]: " + errorThrown);
                } else {
                    alert("Error trap added by demo.ajax. Status [" + XMLHttpRequest.status + "]: " + XMLHttpRequest.responseText);
                }
            };
        }
        return userErrorHandler;
    };
    
    var doAjax = function(params){
        var type = params.type || 'GET';
        var url  = params.url || null;
        var link = params.link || null;
        var data = params.data || null;
        var success = params.success || null;
        var error = params.error || null;
        var cache = params.cache || false;
        var dataType = params.dataType || 'json';
        var contentType = params.contentType || 'application/x-www-form-urlencoded';
        var beforeSend = params.beforeSend || null;
        
        var targetUrl = params.url;
        if(link !== null && link.href){
            targetUrl = link.href;
        }
        if(targetUrl === null){
            throw 'invalid parameters. You must specify either an url or a link';
        }
        
        $.ajax({
            type: type,
            url: targetUrl,
            contentType: contentType,
            beforeSend: beforeSend,
            data: data,
            cache: false,
            success: success,
            error: error,
            dataType: dataType,
            beforeSend: function(xhr){
            	xhr.withCredentials = true;
            }
        });
    };
    
    //
    //public members
    //
    return {
    
        /**
         * executes an ajax GET. Same paramaters as used in JQuery.ajax but with two exceptions. Type is always 'GET' and
         * you can pass either an url or a demo.shared.domain.link but both may not be given
         * 
         *     params = {...
         *               ...
         *               link: hateoasLink
         *                 };
         * 
         * or
         * 
         *     params = {...
         *               ...
         *               url: '/someurl/some/where'
         *                 };
         * 
         */
        get: function(params){
            params.type = 'GET';
            params.error = errorTrap(params.error);
            
            doAjax(params);
        },

        /**
         * @see get.  Always 'POST'
         */
        post: function(params){
            params.type = 'POST';
            params.error = errorTrap(params.error);
            
            doAjax(params);
        },
        
        put: function(params){
            params.type = 'PUT';
            params.error = errorTrap(params.error);
            
            doAjax(params);
        },

        /**
         * @see get.  Always 'DELETE'
         */
        del: function(params){
            params.type = 'DELETE';
            params.error = errorTrap(params.error);
            
            doAjax(params);
        },

        invoke: function(params) {
            params.error = errorTrap(params.error);

            doAjax(params);
        }
    };
    
})($);
