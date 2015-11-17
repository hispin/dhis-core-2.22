var path = require('path');

module.exports = function karmaConfigHandler(config) {
    config.set({
        browsers: [ 'PhantomJS' ], // run in Headless browser PhantomJS
        singleRun: false,
        frameworks: [
            'mocha', // Test runner
            'chai',  // Assertion library
            'sinon', // Mocking library
            'sinon-chai' // Assertions for mocks and spies
        ],
        files: [
            '../node_modules/phantomjs-polyfill/bind-polyfill.js',
            '../../angular/angular.js',
            '../node_modules/angular-mocks/angular-mocks.js',
            '../dhis2.angular.*.js',
            'tests.webpack.js', // just load this file
        ],
        preprocessors: {
            'tests.webpack.js': [ 'webpack', 'sourcemap' ], // preprocess with webpack and our sourcemap loader
            '../dhis2.angular.*.js': ['coverage'],
            // '../views/left-bar.html': ['ng-html2js'], // Example for transforming html files to be angular modules that are cached
        },
        reporters: [ 'dots', 'coverage' ], // report results in this format
        coverageReporter: {
            type: 'lcov',
            dir: '../coverage',
            subdir: function simplifyBrowsername(browser) {
                // normalization process to keep a consistent browser name accross different OS
                return browser.toLowerCase().split(/[ /-]/)[0];
            },
        },
        webpack: { // kind of a copy of your webpack config
            devtool: 'inline-source-map', // just do inline source maps instead of the default
            module: {
                loaders: [
                    { test: /\.js$/, loader: 'babel-loader' },
                ],
            },
        },

        ngHtml2JsPreprocessor: {
            cacheIdFromPath: function(filepath) {
                return filepath.replace(path.normalize(__dirname + '/..') + '/', '');
            },

            moduleName: function (htmlPath, originalPath) {
                return htmlPath;
            }
        },

        webpackServer: {
            noInfo: true, // please don't spam the console when running in karma!
        },
    });
};
