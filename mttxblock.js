/* Javascript for ModelTracerXBlock. */
function ModelTracerXBlock(runtime, element) {


    var handlerUrl = runtime.handlerUrl(element,'load_tutor');
    var iframe = document.getElementById("tutorInterface");
    var username = "";
    var tutorname = "";
    var probselection = "";
    var studentID = "";
    var locationObject;
    var pageID = "";
    var htmlurl;
    var dynamiclink;
    var kc;

     $(function () {
        console.log($('#seq_content').children()[0]);
        locationObject = $('#seq_content').children()[0] == undefined ? undefined : $('#seq_content').children()[0].getAttribute('data-usage-id');
        console.log(" LocationObject : "+locationObject);
        if(locationObject != undefined){
                var locationArray = locationObject.split("@");
                pageID = locationArray[locationArray.length - 1];
                $.ajax({
                    url: runtime.handlerUrl(element, 'module_skill_name_saved'),
                    type: "POST",
                    async: false,
                    data: JSON.stringify({"location_id": locationArray[locationArray.length - 1]}),
                    success: function(data) {
                            console.log("Skillname has been saved!", data);
                    }
                });
        }
     });

    function getArguments(){

    var data = {
        pageid : pageID
    };

      $.ajax({
          type: "POST",
          async: false,
          url: handlerUrl,
          data: JSON.stringify(data),
          success: function(data){
             console.log(" Response : "+data);
             username = data.username;
             tutorname = data.tutorname;
             probselection = data.probselection;
             dynamiclink = data.dynamiclink;
             studentID = data.studentID;
             kc = data.kc;
             console.log(" username : "+username+" tutorname : "+tutorname+" dynamicLink "+dynamiclink);
             iframe.contentWindow.postMessage(data,'*');

          },
          error: function(xhr, options, error){
            console.log("Error : "+xhr.status+" message "+error);
          }
      });
    }

    window.addEventListener('message', function(event){
       console.log(" I have received an message from my child : "+event.data)
       event.data['pageID'] = pageID;
       console.log(" Page ID "+pageID+"  Event : "+event.data['pageID']);

       if(event.data == "shrink")
             shrinkTutor();
       else if( (event.data == "dynamicLinkShrink" || event.data == "dynamicLink" )&& dynamiclink == "1"){
              if(event.data == "dynamicLinkShrink")
                 shrinkTutor();
              console.log(" Exist : "+$("#dynamicLink").length);
              if($("#dynamicLink").length == 0)
                enableDynamicLink()
       }
       else if(event.data == "getArguments")
            getArguments();
       else
            logData(event.data);

    });

    $(element).find("#mainbar").click(function(event){
        if($("#tutorInterface").is(':visible'))
           shrinkTutor();
         else
           expandTutor();
    });

  function shrinkTutor(){
             $("#mainbar span").removeClass("icon fa fa-caret-down");
             $("#mainbar span").addClass("icon fa fa-caret-right");
             $("#interfaceSection").slideUp("slow");
  }

  function expandTutor(){
             $("#mainbar span").removeClass("icon fa fa-caret-right");
             $("#mainbar span").addClass("icon fa fa-caret-down");
             $("#interfaceSection").slideDown("slow");
  }

  function enableDynamicLink(){
     $.ajax({
        type: "POST",
         async : false,
         url : runtime.handlerUrl(element,'get_skill_mapping'),
         data: JSON.stringify(""),
         success: function(data){
             var courseID = data.course_id;
             var paragraphID = data.paragraph_id;
             var locationID = data.location_id;
             var hostname = $(location).attr('host') + "/courses/";
             var url = "'https://" + hostname + courseID+ "/jump_to_id/" + locationID + "#" + paragraphID+ "'";
             $(element).find("#dynamicLinkSection").append("<h7 id='navigate_id'><mark>Click this <a id='dynamicLink' href="+url+">link</a> to review the course content and examples on solving this question.</mark></h7>");
             $("div[id='dynamicLinkSection'] a[id='dynamicLink']").click(function(event){
                    // need an ajax call to update the status as "Resource reviewed"
                    console.log("Dynamic link clicked");
                    $.ajax({
		                type : "GET",
		                async : false,
		                crossDomain : true,
		                url : "https://kona.education.tamu.edu:2401/ALP/updateStatus",
		                data : "status=resourceReviewed"+"&studentID="+studentID+"&tutorname="+tutorname,
		                dataType : "jsonp",
		                jsonp: 'callback',
		                success : function(data) {
			                        console.log("Successfully updated the status ");
			                        var logDetails = {
		                                timestamp : getTimeStamp(),
		                                timezone : "US/Central",
		                                student_response_type : 'HINT',
		                                problem_name : 'N/A',
		                                problem_view : 0,
		                                attempts : 'N/A',
		                                outcome : 'UNGRADED',
		                                selection : 'N/A',
		                                action : 'N/A',
		                                input : 'N/A',
		                                feedback : 'N/A',
		                                help_level : 'N/A',
		                                kc : kc,
		                                cf_field : {
			                                cf_action : 'Dynamic Link Clicked',
			                                cf_result : url
		                                }
	                                };
	                                logData(logDetails);
	                                event.isDefaultPrevented = function(){
	                                   return false;
	                                }
	                                $(this).trigger(event);

		                }
	                });
                     console.log("Ajax call completed");

            });

         },
         error: function(xhr, options, error){
           console.log(" Error : "+xhr.status+" message : "+error)
         }
     });
  }


  function logData(data){
    $.ajax({
         type: "POST",
         async : false,
         url : runtime.handlerUrl(element,'log_data'),
         data: JSON.stringify(data),
         success: function(data){
                console.log("Data logged successfully");
         },
         error: function(xhr, options, error){
           console.log(" Error : "+xhr.status+" message : "+error)
         }
       });
   }

  function getTimeStamp() {
	var currentDate = new Date();
	var date = currentDate.getFullYear() + "-" + (currentDate.getMonth() + 1)+ "-" + currentDate.getDate();
	var currenttime = currentDate.getHours() + ":" + currentDate.getMinutes()+ ":" + currentDate.getSeconds();
	return date + " " + currenttime;
  }
}
