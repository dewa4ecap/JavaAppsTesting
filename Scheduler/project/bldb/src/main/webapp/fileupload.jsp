<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.io.File"%>
html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
<title>Process File Upload</title>
</head>
<%
        System.out.println("Content Type ="+request.getContentType());

        DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(1000000);

        List fileItems = fu.parseRequest(request);
        Iterator itr = fileItems.iterator();

        while(itr.hasNext()) {
          FileItem fi = (FileItem)itr.next();

          //Check if not form field so as to only handle the file inputs
          //else condition handles the submit button input
          if(!fi.isFormField()) {
            System.out.println("\nNAME: "+fi.getName());
            System.out.println("SIZE: "+fi.getSize());
            //System.out.println(fi.getOutputStream().toString());
            //File fNew= new File(application.getRealPath("\"), fi.getName());
			File fNew= new File("c:\\book.xlsx");
            System.out.println(fNew.getAbsolutePath());
            fi.write(fNew);
          }
          else {
            System.out.println("Field ="+fi.getFieldName());
          }
        }
%>
<body>
Upload Successful!!
</body>
</html>
