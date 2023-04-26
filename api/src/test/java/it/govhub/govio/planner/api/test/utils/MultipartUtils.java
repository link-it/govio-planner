package it.govhub.govio.planner.api.test.utils;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import it.govhub.govio.planner.api.test.costanti.Costanti;

public class MultipartUtils {
	
	public static String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
        buffer.append(Costanti.MULTIPART_CHARS[rand.nextInt(Costanti.MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
   }

	public static byte[] createFileContent(byte[] data, String boundary, String contentType, String fileName) {
		return createFileContent(data, boundary, contentType, fileName, Costanti.PART_NAME_FILE);
	}
	
	public static byte[] createFileContent(byte[] data, String boundary, String contentType, String fileName, String partName) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\""+partName+"\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";
        String end = "\r\n--" + boundary + "--";
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }
}
