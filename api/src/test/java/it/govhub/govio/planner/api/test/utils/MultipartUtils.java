/*******************************************************************************
 *  GovIO Planner - Notification system Planner for AppIO
 *  
 *  Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3, as published by
 *  the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
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
