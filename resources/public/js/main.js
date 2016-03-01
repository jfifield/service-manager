$(function() {
  $(".host-status").each(function() {
    $(this).load("/hosts/" + $(this).data("host-id") + "/status");
  });
  $(".host-service-status").each(function() {
    $(this).load("/hosts/" + $(this).data("host-id") + "/services/" + $(this).data("service-id") + "/status");
  });
});
