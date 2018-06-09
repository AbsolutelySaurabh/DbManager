
$(document).ready(function () {

    getDatabaseList();


    $('#sidebarCollapse').on('click', function () {
         $('#sidebar').toggleClass('active');
     });


 });


 function getDatabaseList() {


       $.ajax({url: "getDbList", success: function(result){

                   result = JSON.parse(result);
                   var dbList = result.rows;
                   $('#db-list').empty();
                   var parentElement = $('#db-list');
                   var isSelectionDone = false;
                   for(var count = 0; count < dbList.length; count++){ 


                         var dbName = dbList[count][0];
                         var isEncrypted = dbList[count][1];
                         var isDownloadable = dbList[count][2];
                         var dbAttribute = isEncrypted == "true" ? ' <span class="glyphicon glyphicon-lock" aria-hidden="true" style="color:blue"></span>' : "";
                         if(dbName.indexOf("journal") == -1){


                            $("#db-list").append(
                                "<li>" +
                                   "<a href='#' id=" + dbName + " " + " aria-expanded=" + "false" + " onClick='openDatabaseAndGetTableList(\""+ dbName + "\", \""+ isDownloadable + "\");'>" +
                                        "<i class='glyphicon glyphicon-tasks'></i>" +
                                        dbName +
                                    "</a>" +

                                "</li>");

                         }
                   }

                }
            });
}

function openDatabaseAndGetTableList(db, isDownloadable){

    $.ajax({url: "getTableList?database="+db, success: function(result){

       result = JSON.parse(result);
       var tableList = result.rows;
       var dbVersion = result.dbVersion;

       $('#table-list').empty();
       list = $('#table-list').append("<ul></ul>");
       for(var count = 0; count < tableList.length; count++){

            var tableName = tableList[count];

            list.append("<li ><a href='#'table=" + tableName + " data-db-name=" + db + " data-table-name=" + tableName
               + " onClick= 'getTableData(\"" + tableName + "\");'>" + "<i class='glyphicon glyphicon-list-alt'></i>"  + tableName + "</a></li>");

        }

    }});

}

function getTableData(tableName) {

   $.ajax({url: "getAllDataFromTheTable?tableName="+tableName, success: function(result){

           result = JSON.parse(result);
           inflateData(result);

   }});
}

function inflateData(result){

   if(result.isSuccessful){


      var columnHeader = result.tableInfos;

      // set function to return cell data for different usages like set, display, filter, search etc..
      for(var i = 0; i < columnHeader.length; i++) {
        columnHeader[i]['targets'] = i;
        columnHeader[i]['data'] = function(row, type, val, meta) {
            var dataType = row[meta.col].dataType;
            if (type == "sort" && dataType == "boolean") {
                return row[meta.col].value ? 1 : 0;
            }
            return row[meta.col].value;
        }
      }

       var columnData = result.rows;
       var tableId = "#db-data";

       $("#db-data-div").remove();
       $("#parent-data-div").append('<div id="db-data-div"><table cellpadding="0" cellspacing="0" border="0" class="dataTable table table-striped" id="db-data"></table></div>');

        var availableButtons;
        if (result.isEditable) {
             availableButtons = [
                 {
                     text : 'Add',
                     name : 'add'
                 },
                 {
                     extend: 'selected',
                     text: 'Edit',
                     name: 'edit'
                 },
                 {
                     extend: 'selected',
                     text: 'Delete',
                     name: 'delete'
                 }
             ];
        } else {
             availableButtons = [];
        }

       //for more : https://datatables.net/examples/server_side/ids.html
       //automatics additon of row ID attributes.
       //code reference : http://kingkode.com/free-datatables-editor-alternative/
       $(tableId).dataTable({
           "data": columnData,
           "columnDefs": columnHeader,
           'bPaginate': true,
           'searching': true,
           'bFilter': true,
           'bInfo': true,
           "bSort" : true,
           "scrollX": true,
           "iDisplayLength": 10,
           "dom": "Bfrtip",
            select: 'single',
            altEditor: true,     // Enable altEditor
            buttons: availableButtons
       })

     // hack to fix alignment issue when scrollX is enabled
     $(".dataTables_scrollHeadInner").css({"width":"100%"});
     $(".table ").css({"width":"100%"});

   }else{
      if(!result.isSelectQuery){
         showErrorInfo("Query Execution Failed");
      }else {
         showErrorInfo("Some Error Occurred");
      }
   }
}