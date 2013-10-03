package eu.deib.polimi.csparql_rest_api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EncodingTest {

	public static void main(String[] args) {
		
		String s = "http://ex.org/stream";
		String es = null;
		
		try {
			es = URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(es);
		try {
			System.out.println(URLDecoder.decode(es, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
