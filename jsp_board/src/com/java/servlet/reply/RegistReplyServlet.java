package com.java.servlet.reply;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.dto.ReplyVO;
import com.java.service.ReplyService;
import com.java.service.ReplyServiceImpl;

@WebServlet("/replies/regist")
public class RegistReplyServlet extends HttpServlet {
	
	private ReplyService service = ReplyServiceImpl.getInstance();
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ObjectMapper mapper=new ObjectMapper();
		//System.out.println(request.getReader().readLine());
		
		ReplyVO reply = mapper.readValue(request.getReader(), ReplyVO.class);
		
		//System.out.println(reply);
		PrintWriter out = response.getWriter();		
		
		try {
			service.registReply(reply);			
			out.print("SUCCESS");
		} catch (SQLException e) {
			e.printStackTrace();
			out.print("FAIL");
		}
		out.close();
	}

}



