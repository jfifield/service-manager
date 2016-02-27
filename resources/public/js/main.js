$(function() {
  $(".host-status").each(function() {
    $(this).load("/hosts/" + $(this).data("host-id") + "/status");
  });
});
