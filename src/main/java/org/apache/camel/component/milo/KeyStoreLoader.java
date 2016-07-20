/*
 * Copyright (C) 2016 Jens Reimann <jreimann@redhat.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.milo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class KeyStoreLoader {
	private String type = "PKCS12";
	private URL url;
	private String keyStorePassword;
	private String keyPassword;
	private String keyAlias;

	public static class Result {

		private final X509Certificate certificate;
		private final KeyPair keyPair;

		public Result(final X509Certificate certificate, final KeyPair keyPair) {
			this.certificate = certificate;
			this.keyPair = keyPair;
		}

		public X509Certificate getCertificate() {
			return this.certificate;
		}

		public KeyPair getKeyPair() {
			return this.keyPair;
		}
	}

	public KeyStoreLoader() {
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setUrl(final URL url) {
		this.url = url;
	}

	public URL getUrl() {
		return this.url;
	}

	public void setKeyStorePassword(final String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getKeyStorePassword() {
		return this.keyStorePassword;
	}

	public void setKeyPassword(final String keyPassword) {
		this.keyPassword = keyPassword;
	}

	public String getKeyPassword() {
		return this.keyPassword;
	}

	public void setKeyAlias(final String keyAlias) {
		this.keyAlias = keyAlias;
	}

	public String getKeyAlias() {
		return this.keyAlias;
	}

	public Result load() throws GeneralSecurityException, IOException {

		final KeyStore keyStore = KeyStore.getInstance(this.type);

		try (InputStream stream = this.url.openStream()) {
			keyStore.load(stream, this.keyStorePassword != null ? this.keyStorePassword.toCharArray() : null);
		}

		final Key privateKey = keyStore.getKey(this.keyAlias,
				this.keyPassword != null ? this.keyPassword.toCharArray() : null);

		if (privateKey instanceof PrivateKey) {
			final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(this.keyAlias);
			if (certificate == null) {
				return null;
			}

			final PublicKey publicKey = certificate.getPublicKey();
			final KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) privateKey);
			return new Result(certificate, keyPair);
		}

		return null;
	}
}
