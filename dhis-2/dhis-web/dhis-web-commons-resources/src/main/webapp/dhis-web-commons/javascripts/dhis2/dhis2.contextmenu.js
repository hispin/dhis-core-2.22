/*
 * Copyright (c) 2004-2013, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

dhis2.util.namespace('dhis2.contextmenu');
dhis2.util.namespace('dhis2.contextmenu.utils');

/**
 * Creates a resolver to search within a certain scope
 *
 * @param scope Scope to search within
 * @returns Function
 */
dhis2.contextmenu.utils.findInScope = function( scope ) {
  return function( fnName ) {
    if( typeof scope[fnName] !== 'function' ) {
      throw new Error('target-fn \'' + fnName + '\' does not point to a valid function.')
    }

    return scope[fnName];
  }
};

dhis2.contextmenu.defaultOptions = {
  listId: 'list',
  menuId: 'menu',
  menuItemActiveClass: 'menuItemActive',
  listItemProps: ['id', 'uid', 'name', 'type'],
  functionResolver: dhis2.contextmenu.utils.findInScope(window)
};

dhis2.contextmenu.config = dhis2.contextmenu.defaultOptions;

dhis2.contextmenu.makeContextMenu = function( options ) {
  dhis2.contextmenu.config = $.extend({}, dhis2.contextmenu.defaultOptions, options);
  var config = dhis2.contextmenu.config;

  var $list = $('#' + config.listId);
  var $menu = $('#' + config.menuId);
  var $menuItems = $menu.find('ul');

  // make sure that all old event handler are removed (with .context namespace)
  $(document).off('click.context');
  $list.off('click.context');
  $menuItems.off('click.context');

  $menuItems.on('click.context', 'li', function( e ) {
    var context = {};

    $.each(config.listItemProps, function( idx, val ) {
      context[val] = $menu.data(val);
    });

    var $target = $(e.target);
    var targetFn = $target.data('target-fn');
    var fn = config.functionResolver(targetFn);

    $menu.hide();
    fn(context);

    return false;
  });

  $list.on('click.context', 'td', function( e ) {
    $list.find('td').removeClass(config.menuItemActiveClass);

    if( $menu.is(":visible") ) {
      $menu.hide();
      return false;
    }

    $menu.show();
    $menu.css({left: e.pageX, top: e.pageY});

    var $target = $(e.target);

    $target.addClass(config.menuItemActiveClass);

    $.each(config.listItemProps, function( idx, val ) {
      $menu.data(val, $target.data(val));
    });

    return false;
  });

  $(document).on('click.context', function() {
    dhis2.contextmenu.disable();
    $menu.removeData('id');
  });

  $(document).keyup(function( e ) {
    if( e.keyCode == 27 ) {
      dhis2.contextmenu.disable();
    }
  });
};

dhis2.contextmenu.disable = function() {
  var config = dhis2.contextmenu.config;

  var $list = $('#' + config.listId);
  var $menu = $('#' + config.menuId);

  if( $menu.is(":visible") ) {
    $menu.hide();
  }

  $list.find('td').removeClass(config.menuItemActiveClass);
};
