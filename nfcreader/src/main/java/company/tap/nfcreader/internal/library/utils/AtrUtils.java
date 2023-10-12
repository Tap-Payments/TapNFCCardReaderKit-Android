/*
 * Copyright (C) 2013 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package company.tap.nfcreader.internal.library.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import company.tap.nfcreader.R;
import company.tap.nfcreader.internal.library.log.Logger;
import company.tap.nfcreader.internal.library.log.LoggerFactory;

import static company.tap.nfcreader.internal.library.utils.CommonsUtils.UTF_8;
import static company.tap.nfcreader.internal.library.utils.CommonsUtils.deleteWhitespace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;


/**
 * Class used to find ATR description
 * 
 * @author Millau Julien
 * 
 */
public final class AtrUtils {
	static ArrayList<String> ret = new ArrayList<>();
	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AtrUtils.class);

	/**
	 * MultiMap containing ATR
	 */
	private static final Multimap<String, String> MAP = Multimap.create();

	static {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		try {
			is = AtrUtils.class.getResourceAsStream("/res/raw/smartcard_list.txt");
			isr = new InputStreamReader(is, UTF_8);
			br = new BufferedReader(isr);

			int lineNumber = 0;
			String line;
			String currentATR = null;
			while ((line = br.readLine()) != null) {
				++lineNumber;
				if (line.startsWith("#") || line.trim().length() == 0) { // comment ^#/ empty line ^$/
					continue;
				} else if (line.startsWith("\t") && currentATR != null) {
					MAP.putElement(currentATR, line.replace("\t", "").trim());
				} else if (line.startsWith("3")) { // ATR hex
					currentATR = deleteWhitespace(line.toUpperCase());
				} else {
					LOGGER.error("Encountered unexpected line in atr list: currentATR=" + currentATR + " Line(" + lineNumber
							+ ") = " + line);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			CommonsUtils.closeQuietly(br);
			CommonsUtils.closeQuietly(isr);
			CommonsUtils.closeQuietly(is);
		}
	}

	/**
	 * Method used to find description from ATR
	 * 
	 * @param pAtr
	 *            Card ATR
	 * @return list of description
	 */
	@SuppressWarnings("unchecked")
	public static final Collection<String> getDescription(final String pAtr) {
		Collection<String> ret = null;
		if (CommonsUtils.isNotBlank(pAtr)) {
			String val = deleteWhitespace(pAtr);
			for (String key : MAP.keySet()) {
				if (val.matches("^" + key + "$")) {
					ret = (Collection<String>) MAP.get(key);
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Method used to find ATR description from ATS (Answer to select)
	 * 
	 * @param pAts
	 *            EMV card ATS
	 * @return card description
	 */

	public static  ArrayList<String> getDescriptionFromAts(final String pAts) {
	if(ret!=null) ret = new ArrayList<>();
		if (StringUtils.isNotBlank(pAts)) {
			String val = StringUtils.deleteWhitespace(pAts).replaceAll("9000$", "");
			for (String key : MAP.keySet()) {
				int j = val.length() - 1;
				int i = key.length() - 1;
				while (i >= 0) {
					if (key.charAt(i) == '.' || key.charAt(i) == val.charAt(j)  ){
						j--;
						i--;
						if (j < 0){
							if (!key.substring(key.length() - val.length(), key.length()).replace(".", "").isEmpty()){
								ret.addAll(Objects.requireNonNull(MAP.get(key)));
							}
							break;
						}
					} else if (j != val.length() - 1) {
						j = val.length() - 1;
					} else if (i == key.length() - 1){
						break;
					} else {
						i--;
						break;
					}
				}
			}
		}
		return ret;

	}

	/**
	 * Private constructor
	 */
	private AtrUtils() {
	}
}
