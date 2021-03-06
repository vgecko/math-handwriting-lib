package me.scai.parsetree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;

import me.scai.handwriting.TokenDegeneracy;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;


public class TerminalSet {
	/* Constants */
	public final static String commentString = "#";
	
	public final static String terminalNameTypePrefix = "TERMINAL(";
	public final static String terminalNameTypeSuffix = ")";
	
	/* Members */
	Map<String, String []> type2TokenMap = new HashMap<>();

	Map<String, List<String>> token2TypesMap = new HashMap<>();

	Map<String, String> token2TexNotationMap = new HashMap<>();
	
	private TokenDegeneracy tokenDegen;
	
	/* Constructor */
	/* Default constructor */
	public TerminalSet() {}
	
	public void readFromJsonAtUrl(URL tsFileUrl)
			throws IOException {		
		String [] lines = null;
		try {			
			lines = TextHelper.readLinesTrimmedNoCommentFromUrl(tsFileUrl, commentString);
		} catch ( Exception e ) {
			throw new IOException("Failed to read terminal set from URL: \"" + tsFileUrl + "\"");
		}

		/* Concatenate the lines to a single string */
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; ++i) {
			sb.append(lines[i]);
			sb.append("\n");
		}


		JsonObject obj = new JsonParser().parse(sb.toString()).getAsJsonObject();

		/* Read the terminals and their types */
		JsonObject termsObj = obj.get("terminals").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : termsObj.entrySet()) {
			String typeName = (String) entry.getKey();
			
			JsonArray termsArray = entry.getValue().getAsJsonArray();
			
			Iterator<JsonElement> termsIt = termsArray.iterator();
			List<String> lstTerms = new ArrayList<String>();
			while (termsIt.hasNext()) {
				String termName = termsIt.next().getAsString();
				lstTerms.add(termName);
			}
			
			/* Add to type-to-token map */
			String [] terms = new String[lstTerms.size()];
			lstTerms.toArray(terms);
			type2TokenMap.put(typeName, terms);
			
			/* Add to token-to-type map */
			for (int j = 0; j < terms.length; ++j) {
                if (token2TypesMap.containsKey(terms[j])) {
                    token2TypesMap.get(terms[j]).add(typeName);
                } else {
                    List<String> typeNames = new ArrayList<>();
                    typeNames.add(typeName);

                    token2TypesMap.put(terms[j], typeNames);
                }

//				token2TypesMap.put(terms[j], typeName);
			}
		}

		/* Read the TeX notations */
		JsonObject texObj = obj.get("texNotations").getAsJsonObject();		
		for (Map.Entry<String, JsonElement> entry : texObj.entrySet()) {
			String termName = entry.getKey();
			String texNotation = entry.getValue().getAsString();
			
			token2TexNotationMap.put(termName, texNotation);
		}

		JsonObject tokenDegenObj = obj.get("tokenDegeneracy").getAsJsonObject();

		tokenDegen = new TokenDegeneracy(tokenDegenObj);

	}
		
	/* Get the type of a token */
	public List<String> getTypeOfToken(String token) {
		return token2TypesMap.get(token);
	}
	
	/* Test if a type is a terminal type */
	public boolean isTypeTerminal(String type) {
//		if ( type == epsString )
//			return true; /* EPS is a terminal. */
//		else			
		if (type2TokenMap.keySet().contains(type)) {
			return true;
		}
		else {
			if (isTerminalNameType(type)) { /* TODO: Use regex */
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/* Test if a token belongs to a terminal type */
	public boolean isTokenTerminal(String token) {
		 return token2TypesMap.keySet().contains(token);
	}
	
	/* Determine whether a token matches a type. Normally, this just entails
	 * a lookup for the type of the token. But a special case is where the 
	 * type is something like "TERMINAL(l)"
	 */
	public boolean match(String tokenName, String typeName) {
		if (isTerminalNameType(typeName)) {
			return terminalNameTypeMatches(typeName, tokenName);
		}
		else {
			List<String> tTokenTypes = getTypeOfToken(tokenName);

            boolean isMatch = false;

            if (tTokenTypes != null) {
                for (String tTokenType : tTokenTypes) {
                    if (typeName.equals(tTokenType)) {
                        return true;
                    }
                }
            }

            return isMatch;
		}
	}
	
	public static boolean isTerminalNameType(String typeName) {
		return typeName.startsWith(terminalNameTypePrefix) && 
			   typeName.endsWith(terminalNameTypeSuffix);
	}
	
	public static String getTerminalNameTypeTokenName(String typeName) {
		return typeName.replace(terminalNameTypePrefix, "").replace(terminalNameTypeSuffix, "");
	}
	
	public boolean terminalNameTypeMatches(String terminalNameType, String tokenName) {
		String tTokenName = getTerminalNameTypeTokenName(terminalNameType);
				
		if (tTokenName.equals(tokenName)) {
			return true;
		}
		else {
			/* Look up the degeneracy table to find alternatives, e.g., "o" for "0"*/
			Set<String> altTokenNames = tokenDegen.getAlternatives(tokenName);
			
			if (altTokenNames != null) {
				return altTokenNames != null && altTokenNames.contains(tTokenName);
			}
			else {
				return false;
			}
		}
	}
	
	
	public boolean typeListContains(List<String> types, String tokenName) {
		Iterator<String> typesIt = types.iterator();		
		while (typesIt.hasNext()) {
			String tType = typesIt.next();
			
			if (isTerminalNameType(tType)) {
				if (terminalNameTypeMatches(tType, tokenName)) {
					return true;
				}
			}
		}
		
		List<String> tTokenTypes = getTypeOfToken(tokenName);

        for (String tTokenType : tTokenTypes) {
            if (types.contains(tTokenType)) {
                return true;
            }
        }

        return false;

	}
	
	/* Factory method: From a JSON file */
	public static TerminalSet createFromJsonAtUrl(URL tsJsonFileUrl) 
		throws Exception {
		TerminalSet ts = new TerminalSet();
		
		try {
			ts.readFromJsonAtUrl(tsJsonFileUrl);
		}
		catch ( Exception e ) {
			throw new Exception("Failed to create TerminalSet from JSON file at URL: \"" + tsJsonFileUrl + "\"");
		}
		
		return ts;
	}

	
}