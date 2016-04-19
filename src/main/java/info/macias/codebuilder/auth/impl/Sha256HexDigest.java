package info.macias.codebuilder.auth.impl;

import info.macias.codebuilder.auth.Digester;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * Using a java class since Kotlin's problems for bitwise byte operations
 *
 * @author Mario Macias (http://github.com/mariomac)
 */
public class Sha256HexDigest implements Digester {
	private static final String HASHING_ALGORITHM = "SHA-256";

	@NotNull
	@Override
	public String digest(@NotNull String input, @NotNull byte[] salt) {
		try {
			final MessageDigest digester = MessageDigest.getInstance(HASHING_ALGORITHM);
			final byte[] stringBytes = input.getBytes();
			final byte[] digestSource = new byte[salt.length + stringBytes.length];
			System.arraycopy(salt,0,digestSource,0,salt.length);
			System.arraycopy(stringBytes,0,digestSource,salt.length,stringBytes.length);

			return encode(digester.digest(digestSource));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static final char[] HEX = "0123456789abcdef".toCharArray();
	@NotNull
	@Override
	public String encode(@NotNull byte[] bytes) {
		final char[] chars = new char[bytes.length * 2];
		for(int i = 0 ; i < bytes.length ; i++) {
			chars[i*2+1] = HEX[bytes[i] & 0xF];
			chars[i*2] = HEX[(bytes[i] >> 4) & 0xF];
		}
		return new String(chars);
	}
}
