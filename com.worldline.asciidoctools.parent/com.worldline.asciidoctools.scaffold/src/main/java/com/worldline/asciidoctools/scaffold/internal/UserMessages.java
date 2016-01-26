/**
 * AsciidocTools by Worldline
 *
 * Copyright (C) 2016 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
/**
 *
 */
package com.worldline.asciidoctools.scaffold.internal;

import java.util.Locale;
import java.util.ResourceBundle;

import java.text.MessageFormat;

/**
 * Enumeration containing internationalisation-related messages and API.
 * 
 * @generated com.worldline.awltech.i18ntools.wizard
 */
public enum UserMessages {
	TITLE("TITLE"), TASK("TASK"), SUBTASK_1("SUBTASK_1"), SUBTASK_2("SUBTASK_2"), SUBTASK_3("SUBTASK_3"), SUBTASK_DONE("SUBTASK_DONE"), CANCEL_POMNOTFOUND("CANCEL_POMNOTFOUND"), CANCEL_POMDOESNTEXIST("CANCEL_POMDOESNTEXIST"), CANCEL_POMNOTPARSEABLE("CANCEL_POMNOTPARSEABLE"), EXCEPTION_FILESTRUCTURE("EXCEPTION_FILESTRUCTURE"), EXCEPTION_XMLREAD("EXCEPTION_XMLREAD"), EXCEPTION_POMUPDATE("EXCEPTION_POMUPDATE")
	;

	/*
	 * Value of the key
	 */
	private final String messageKey;

	/*
	 * Constant ResourceBundle instance
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("UserMessages", Locale.getDefault());

	/**
	 * Private Enumeration Literal constructor
	 * 
	 * @param messageKey
	 *            value
	 */
	private UserMessages(final String messageKey) {
		this.messageKey = messageKey;
	}

	/**
	 * @return the message associated with the current value
	 */
	public String value() {
		if (UserMessages.RESOURCE_BUNDLE == null || !UserMessages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return UserMessages.RESOURCE_BUNDLE.getString(this.messageKey);
	}

	/**
	 * Formats and returns the message associated with the current value.
	 * 
	 * @see java.text.MessageFormat
	 * @param parameters
	 *            to use during formatting phase
	 * @return formatted message
	 */
	public String value(final Object... args) {
		if (UserMessages.RESOURCE_BUNDLE == null || !UserMessages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return MessageFormat.format(UserMessages.RESOURCE_BUNDLE.getString(this.messageKey), args);
	}

}
