rest = window.rest || {};

demo.rest = demo.rest || (function($) {
        
    var doAjax = function(params){
    };
    var decorateHateoas = function(url, obj){
    	if (obj.resources) {
	 	   obj.resources.forEach(function(resource) {
	 		   obj[resource] = function(successFunction)  {
				   demo.ajax.get({url: url + resource + "/",
					   success: function(result) {
						   successFunction(decorateHateoas(url + resource + "/", result));
					   }
				   });
			   }
		   });
    	}
    	if (obj.commands) {
		   obj.commands.forEach(function(command) {
			   alert(command.args);
			   obj[command.name] = function(data, successFunction)  {
				   demo.ajax.post({url: url + command.name,
					   success: successFunction,
					   data: data
				   });
					   // TODO: handle arguments
			   }
		   });
    	}
	   return obj;
    };
    //
    //public members
    //
    return {
        init: function(url,successFunction){
        	demo.ajax.get({url: url,
     	       async: false,
     	       success: function(root){
     	    	   successFunction(decorateHateoas(url, root));
     	       } 
        	});
        }
    };
})($);
