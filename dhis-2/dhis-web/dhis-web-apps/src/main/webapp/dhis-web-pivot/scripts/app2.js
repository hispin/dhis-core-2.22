Ext.onReady( function() {
	var N = PT;

    //NS.Viewport

    // RequestManager
    (function() {
        var RequestManager = N.Api.RequestManager = function(config) {
            var t = this;

            config = NS.isObject(config) ? config : {};

            // constructor
            t.requests = NS.isArray(config.requests) ? config.requests : [];

            t.responses = [];

            t.fn = NS.isFunction(config.fn) ? config.fn : null;
        };

        RequestManager.prototype.add = function(request) {
            this.requests.push(request);
        };

        RequestManager.prototype.set = function(fn) {
            this.fn = fn;
        };

        RequestManager.prototype.ok = function(xhr, suppress) {
            this.responses.push(xhr);

            if (!suppress) {
                this.resolve();
            }
        };

        RequestManager.prototype.resolve = function() {
            if (this.responses.length === this.requests.length) {
                this.fn();
            }
        };
    })();
});
