/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
package com.floreantpos.ui.views.payment;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import com.floreantpos.Messages;
import com.floreantpos.config.CardConfig;
import com.floreantpos.model.PaymentTypeWallet;
import com.floreantpos.swing.DoubleTextField;
import com.floreantpos.swing.FixedLengthTextField;
import com.floreantpos.swing.QwertyKeyPad;
import com.floreantpos.ui.dialog.OkCancelOptionDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.google.gson.JsonObject;

public class DWalletDialog extends OkCancelOptionDialog {
	private FixedLengthTextField tfQRTest;
	private FixedLengthTextField tfTEST;
	//private DoubleTextField tfFaceValue;
	//private QwertyKeyPad qwertyKeyPad;
	
	private JComboBox cbPaymentType;
	
	private String token;

	public DWalletDialog() {
		super();
		
		token = getToken();
		
		System.out.println(token);

		setTitle("DWallet"); //$NON-NLS-1$
		setTitlePaneText("Digital Wallet");

		JPanel panel = getContentPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[][grow]", "[][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		
		JLabel lblSelectPaymentType = new JLabel("Payment Type"); //$NON-NLS-1$
		panel.add(lblSelectPaymentType, "cell 0 0,alignx leading"); //$NON-NLS-1$
		
		cbPaymentType = new JComboBox();
		cbPaymentType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//updateCheckBoxes();
			}
		});
		add(cbPaymentType, "cell 1 0,growx"); //$NON-NLS-1$
		cbPaymentType.setModel(new DefaultComboBoxModel<PaymentTypeWallet>(PaymentTypeWallet.values()));
		

		JLabel lblGiftCertificateNumber = new JLabel("QR"); //$NON-NLS-1$
		panel.add(lblGiftCertificateNumber, "cell 0 1,alignx trailing"); //$NON-NLS-1$
		
		
		JPopupMenu menu = new JPopupMenu();
		Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, "Paste");
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        menu.add( paste );
		
        tfQRTest = new FixedLengthTextField();
        tfQRTest.setLength(300);
        tfQRTest.setComponentPopupMenu( menu );
		
		panel.add(tfQRTest, "cell 1 1,growx"); //$NON-NLS-1$

//		JLabel lblFaceValue = new JLabel(Messages.getString("GiftCertDialog.8")); //$NON-NLS-1$
//		panel.add(lblFaceValue, "cell 0 1,alignx trailing"); //$NON-NLS-1$

//		tfFaceValue = new DoubleTextField();
//		tfFaceValue.setText("50"); //$NON-NLS-1$
//		panel.add(tfFaceValue, "cell 1 1,growx"); //$NON-NLS-1$
//
//		qwertyKeyPad = new QwertyKeyPad();
//		panel.add(qwertyKeyPad, "newline, gaptop 10px, span"); //$NON-NLS-1$
	}

	@Override
	public void doOk() {
		if (StringUtils.isEmpty(getQR())) {
			POSMessageDialog.showMessage("Not QR readed"); //$NON-NLS-1$
			return;
		}
		
		if(!postTransaction()) {
			return;
		}

		setCanceled(false);
		dispose();
	}

	public String getQR() {
		return tfQRTest.getText();
	}
	
	public String getToken() {
		JsonObject json = new JsonObject();
		json.addProperty("email", CardConfig.getDWalletEmail());    
		json.addProperty("password", CardConfig.getDWalletPassword());    
		
		String postUrl = "https://dwallet.survivorsofmars.tk/api/v1/auth/login";
		Pattern tokenPattern = Pattern.compile(".*?\"token\"\\s*:\\s*\"([^\"]+)\".*");
		String token = null;
		try {
			URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = IOUtils.toString(in, "UTF-8");

            in.close();
            conn.disconnect();
            
	        Matcher matcher = tokenPattern.matcher(response);
	        if (matcher.matches() && matcher.groupCount() > 0) {
	        	token = matcher.group(1);
	        }

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	public boolean postTransaction() {
		String dWalletUser = getQR();
		String paymentTypeWallet = CardConfig.getDWalletPaymentType();
		
		JsonObject json = new JsonObject();
		json.addProperty("description", "FloreantPOS");    
		json.addProperty("amount", "-20.00");    
		json.addProperty("type", paymentTypeWallet);    
		json.addProperty("purchaseDate", LocalDate.now().toString());    
		json.addProperty("qr", dWalletUser);    
		
		String postUrl = "https://dwallet.survivorsofmars.tk/api/v1/transaction/qr";
		try {
			URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(20000);
            conn.setRequestProperty("Authorization", "Bearer " + token );
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = IOUtils.toString(in, "UTF-8");
            
            System.out.println(response);

            in.close();
            conn.disconnect();
            

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
		
	}

}
