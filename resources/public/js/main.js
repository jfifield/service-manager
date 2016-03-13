$(function() {
  $(".host-status").each(function() {
    $(this).load("/hosts/" + $(this).data("host-id") + "/status");
  });
  $(".host-service-status").each(function() {
    $(this).load("/hosts/" + $(this).data("host-id") + "/services/" + $(this).data("service-id") + "/status");
  });
  function getServiceControlHandler(action, loadingStatus) {
    return function() {
      var hostId = $(this).data("host-id");
      var serviceId = $(this).data("service-id");
      var statusEl = $(".host-service-status[data-host-id='" + hostId + "'][data-service-id='" + serviceId + "']");
      statusEl.html("<span class='text-warning'><span class='glyphicon glyphicon-question-sign'/> " + loadingStatus + "...</span>");
      $.post("/hosts/" + hostId + "/services/" + serviceId + "/" + action, function(data) {
        statusEl.html(data);
      });
    };
  }
  $(".host-service-start").click(getServiceControlHandler("start", "Starting"));
  $(".host-service-stop").click(getServiceControlHandler("stop", "Stopping"));
});
