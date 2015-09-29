/* global Java*/
define('logger', function(){
    var ScriptsClass = Java.type("com.eas.script.Scripts");
    var ScriptedResourceClass = Java.type("com.eas.client.scripts.ScriptedResource");
    var JavaStringArrayClass = Java.type("java.lang.String[]");
    var space = ScriptsClass.getSpace();
    /**
     * Constructs server module network proxy.
     * @constructor
     * @param {String} aModuleName Name of server module (session stateless or statefull or rezident).
     */
    function RPCProxy(aModuleName) {
        if (aModuleName) {
            var app = ScriptedResourceClass.getApp();
            if (app) {
                var proxy = app.getServerModules();
                if (proxy) {
                    var moduleInfo = proxy.getCachedStructure(aModuleName);
                    if (moduleInfo.isPermitted()) {
                        var functions = moduleInfo.getFunctionsNames();
                        var currentObject = this;
                        functions.forEach(function (aFunctionName) {
                            currentObject[aFunctionName] = function () {
                                var onSuccess = null;
                                var onFailure = null;
                                var argsLength = arguments.length;
                                while (argsLength > 0 && !arguments[argsLength - 1]) {
                                    argsLength--;
                                }
                                if (argsLength > 1 && typeof arguments[argsLength - 1] === "function" && typeof arguments[argsLength - 2] === "function") {
                                    onSuccess = arguments[argsLength - 2];
                                    onFailure = arguments[argsLength - 1];
                                    argsLength -= 2;
                                } else if (argsLength > 0 && typeof arguments[argsLength - 1] === "function") {
                                    onSuccess = arguments[argsLength - 1];
                                    argsLength -= 1;
                                }
                                var params = new JavaArrayClass(argsLength);
                                for (var j = 0; j < argsLength; j++) {
                                    params[j] = arguments[j];
                                }
                                if (onSuccess) {
                                    proxy.callServerModuleMethod(aModuleName, aFunctionName, space, onSuccess, onFailure, params);
                                } else {
                                    var result = proxy.callServerModuleMethod(aModuleName, aFunctionName, space, null, null, params);
                                    return result && result.getPublished ? result.getPublished() : result;
                                }
                            };
                        });
                    } else {
                        throw 'Access denied for module "' + aModuleName + '". May be denied public access.';
                    }
                } else {
                    throw 'This architecture does not support server modules.';
                }
            }
        } else {
            throw "Module name could not be empty.";
        }
    }
    function loadRemotes(aRemotesNames, aOnSuccess, aOnFailure) {
        var remotesNames = Array.isArray(aRemotesNames) ? aRemotesNames : [aRemotesNames];
        ScriptedResourceClass.loadRemotes(Java.to(remotesNames, JavaStringArrayClass), aOnSuccess ? aOnSuccess : null, aOnFailure ? aOnFailure : null);
    }
    var module = {};
    Object.defineProperty(module, 'Proxy', {
        enumerable: true,
        value: RPCProxy
    });
    Object.defineProperty(module, 'loadRemotes', {
        enumerable: true,
        value: loadRemotes
    });
    return module;
});