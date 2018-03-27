/* Javascript for ModelTracerXBlock. */
function ModelTracerXBlockStudio(runtime, element) {

  $(element).find('.save-button').bind('click', function() {
     var handleUrl = runtime.handlerUrl(element,'studio_save');
     var data = {
          display_name : $(element).find('input#display_name').val(),
          tutorname : $(element).find('input#tutorname').val(),
          skillname : $(element).find('input#skillname').val(),
          username : $(element).find('input#username').val(),
          htmlurl : $(element).find('input#htmlurl').val(),
          hintoptions : $(element).find('input#hintoptions').val(),
          probselection : $(element).find('input#probselection').val(),
          typechecker : $(element).find('input#typechecker').val(),
          inputmatcher : $(element).find('input#inputmatcher').val(),
          section : document.getElementsByClassName("navigation-parent")[0].innerText,
          subsection : document.getElementsByClassName("navigation-parent")[1].innerText,
          unit: $("span[class='title-value']").text()
      };
      var message = validate(data);
      if(message == "success"){
          if(runtime.notify)
            runtime.notify('save',{state:'start'});
          $.ajax({
            url:handleUrl,
            method: "POST",
            async: false,
            data: JSON.stringify(data),
            success: function(response){
               console.log(" Response : "+response);
            }
          });
          if(runtime.notify)
          runtime.notify('save',{state:'end'});
      }
      else
          alert(message);
  });

  $(element).find('.cancel-button').bind('click',function(){
     runtime.notify('cancel',{});
  });

  function validate(data){
     if(data.tutorname.length == 0 || $.type(data.tutorname) !== 'string')
        return "Invalid tutor name";
     else if(data.username.length == 0 || $.type(data.username) !== 'string')
        return "Invalid username";
     else if(data.htmlurl.length == 0)
        return "Invalid html url";
     else if(data.hintoptions.length == 0 || $.type(data.hintoptions) !== 'string' || (data.hintoptions != 'enable' && data.hintoptions != 'disable'))
        return "Invalid hint options, you can give either enable or disable "+data.hintoptions;
     else if(data.probselection.length == 0 || $.type(data.probselection) !== 'string')
        return "Invalid problem selection criteria";
     else
        return "success";
  }

}
