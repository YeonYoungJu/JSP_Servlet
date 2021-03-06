package com.java.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.java.dto.AttachVO;
import com.java.service.AttachServiceImpl;

//@WebServlet("/upload")
public class FileUploadServlet extends HttpServlet {

	// location to store file uploaded
	private static final String UPLOAD_DIRECTORY = "upload";
	private String rootDrive;
	private List<String> rootPath;

	@Override
	public void init(ServletConfig config) throws ServletException {
		rootDrive = config.getInitParameter("rootDrive");
		rootPath = Arrays.asList(config.getInitParameter("rootPath").split("\\\\"));
		System.out.println("rootDrive : " + rootDrive);
		System.out.println("rootPath : " + rootPath);
	}

	// 업로드 파일 환경 설정
	private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3; // 3MB
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String url = "/WEB-INF/views/fileUploadForm.jsp";
		request.getRequestDispatcher(url).forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// request 파일 첨부 여부 확인.
		if (!ServletFileUpload.isMultipartContent(request)) {
			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.println("alert('multipart/form-data 형식이 아닙니다.')");
			out.println("</script>");
			out.flush();
			out.close();
			return;
		}

		// 업로드를 위한 upload 환경설정 적용.
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 저장을 위한 threshold memory 적용.
		factory.setSizeThreshold(MEMORY_THRESHOLD);
		// 임시 저장 위치 결정.
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
		ServletFileUpload upload = new ServletFileUpload(factory);

		// 업로드 파일의 크기 적용.
		upload.setFileSizeMax(MAX_FILE_SIZE);

		// 업로드 request 크기 적용.
		upload.setSizeMax(MAX_REQUEST_SIZE);

		// 실제 저장 경로를 설정.
		// String uploadPath = getServletContext().getRealPath("") + UPLOAD_DIRECTORY;
		String uploadPath = rootDrive;

		for (String path : rootPath) {
			File filePath = new File(uploadPath + File.separator + path);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			uploadPath += File.separator + path;
		}
		uploadPath += File.separator+UPLOAD_DIRECTORY;		

		// 실제 저장 경로를 생성(없으면)
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}

		// request로 부터 받는 파일을 추출해서 저장.
		try {
			List<FileItem> formItems = upload.parseRequest(request);

			// 파일 개수 확인.
			if (formItems != null && formItems.size() > 0) {
				List<AttachVO> attachList = new ArrayList<AttachVO>();
				String writer = null; // request.getParameter("writer")
				String title = null; // request.getParameter("title");
				String content = null;// request.getParameter("content");
				int pno = -1; // Integer.parseInt(request.getParameter("pno"))
				for (FileItem item : formItems) {
					if (!item.isFormField()) {
						String fileName = new File(item.getName()).getName();
						String filePath = uploadPath + File.separator + fileName;
						File storeFile = new File(filePath);

						// local HDD 에 저장.
						item.write(storeFile);

						// DB에 저장할 attach에 file 내용 추가.
						AttachVO attach = new AttachVO();
						attach.setFileName(fileName);
						attach.setUploadPath(uploadPath);
						attach.setFileType(fileName.substring(fileName.lastIndexOf(".") + 1));

						attachList.add(attach);

						System.out.println("upload file : " + attach);

						request.setAttribute("message", "업로드 되었습니다.");
					} else {
						switch (item.getFieldName()) {
						case "title":
							title = item.getString("utf-8");
							break;
						case "writer":
							writer = item.getString("utf-8");
							break;
						case "content":
							content = item.getString("utf-8");
							break;
						case "pno":
							pno = Integer.parseInt(item.getString("utf-8"));
							break;
						}
					}
				}
				// DB에 저장.......
				for (AttachVO attach : attachList) {
					attach.setAttacher(writer);
					attach.setPno(pno);
					AttachServiceImpl.getInstance().addAttach(attach);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("message", "파일 업로드에 실패했습니다.");
		}

		String url = "/WEB-INF/views/fileUploaded.jsp";
		request.getRequestDispatcher(url).forward(request, response);

	}
}
