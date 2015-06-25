var OAuth2Service = {
  save: function(o, id) {
    if( !id ) {
      return $.ajax({
        url: '../api/oAuth2Clients/' + id,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(o)
      });
    } else {
      return $.ajax({
        url: '../api/oAuth2Clients',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(o)
      });
    }
  },
  load: function(id) {
    return $.ajax({
      url: '../api/oAuth2Clients/' + id
    })
  },
  fromJson: function(o) {
    $('#name').val(o.name);
    $('#clientId').val(o.cid);
    $('#clientSecret').val(o.secret);
  },
  toJson: function() {
    var o = {};

    o.name = $('#name').val();
    o.cid = $('#clientId').val();
    o.secret = $('#clientSecret').val();

    return o;
  },
  getUuid: function() {
    var def = $.Deferred();

    $.ajax({
      url: '../api/system/uuid',
      dataType: 'json'
    }).done(function(o) {
      def.resolve(o.codes[0]);
    });

    return def.promise();
  }
};

function updateO2Client(context) {
  location.href = 'getOAuth2Client.action?id=' + context.uid;
}

function deleteO2Client(context) {
  if( window.confirm(i18n_confirm_delete) ) {
    $.ajax({
      url: '../api/oAuth2Clients/' + context.uid,
      type: 'DELETE'
    }).done(function() {
      location.reload();
    });
  }
}
