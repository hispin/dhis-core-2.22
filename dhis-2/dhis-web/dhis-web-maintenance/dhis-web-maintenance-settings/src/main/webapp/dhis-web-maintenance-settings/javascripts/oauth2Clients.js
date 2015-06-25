function updateO2Client(context) {
  console.log(context);
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