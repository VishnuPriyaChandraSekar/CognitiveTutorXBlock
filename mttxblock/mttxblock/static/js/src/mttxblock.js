/* Javascript for ModelTracerXBlock. */
function ModelTracerXBlock(runtime, element) {


    var handlerUrl = runtime.handlerUrl(element,'load_tutor');
    var iframe = document.getElementById("tutorInterface");
    var username = "";
    var tutorname = "";
    var probselection = "";
    var studentID = "";

    $("#tutorInterface").on("load",function(){
       alert(" Loading the tutor interface");

       getArguments();

    });

    function getArguments(){
      $.ajax({
          type: "POST",
          async: false,
          url: handlerUrl,
          data: JSON.stringify(''),
          success: function(data){
             console.log(" Response : "+data);
             username = data.username;
             tutorname = data.tutorname;
             probselection = data.probselection;
             alert(" username : "+username+" tutorname : "+tutorname);
             iframe.contentWindow.postMessage(data,'*');

          },
          error: function(xhr, options, error){
            console.log("Error : "+xhr.status+" message "+error);
          }
      });
    }

    window.addEventListener('message', function(event){
       console.log(" I have received an message from my child : "+event.data)
       $.ajax({
         type: "POST",
         async : false,
         url : runtime.handlerUrl(element,'log_data'),
         data: JSON.stringify(event.data),
         success: function(data){
            console.log(" successfully stored the data in the database : "+data.success+ " selection : "+data.selection);
         },
         error: function(xhr, options, error){
           console.log(" Error : "+xhr.status+" message : "+error)
         }
       })
    });

}
