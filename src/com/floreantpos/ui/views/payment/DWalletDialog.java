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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.gson.JsonObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class DWalletDialog extends OkCancelOptionDialog {
	private FixedLengthTextField tfQRTest;
	private FixedLengthTextField tfTEST;
	//private DoubleTextField tfFaceValue;
	//private QwertyKeyPad qwertyKeyPad;
	
	private JComboBox cbPaymentType;
	private WebcamPanel webcamPanel;
	private Webcam webcam;
	
	
	private String token;
	private String QR;
	JLabel lblStatus;
	JLabel lblBalance;
	JLabel lblAmount;
	
	private double tenderAmount;
	private double pointsAmount;
	private String amount;

	public DWalletDialog(double tenderAmount) {
		super();
		this.tenderAmount = tenderAmount;
		amount = String.valueOf(tenderAmount);
		pointsAmount = tenderAmount*Double.valueOf(CardConfig.getDWalletPointConversion());
				
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
				updateLabelsAmount();
			}
		});
		panel.add(cbPaymentType, "cell 1 0,growx"); //$NON-NLS-1$
		cbPaymentType.setModel(new DefaultComboBoxModel<PaymentTypeWallet>(PaymentTypeWallet.values()));
		

		JLabel lblTextQR = new JLabel("QR"); //$NON-NLS-1$
		panel.add(lblTextQR, "cell 0 1,alignx trailing"); //$NON-NLS-1$
		
		
		JPopupMenu menu = new JPopupMenu();
		Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, "Paste");
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        menu.add( paste );
		
        tfQRTest = new FixedLengthTextField();
        tfQRTest.setLength(300);
        tfQRTest.setComponentPopupMenu( menu );
		panel.add(tfQRTest, "cell 1 1,growx"); //$NON-NLS-1$
		
		lblBalance = new JLabel("Balance: "); //$NON-NLS-1$
		panel.add(lblBalance, "cell 0 2,alignx trailing"); //$NON-NLS-1$
		
		lblAmount = new JLabel(String.valueOf(tenderAmount)); //$NON-NLS-1$
		panel.add(lblAmount, "cell 1 2,alignx trailing"); //$NON-NLS-1$
		
		webcam = Webcam.getDefault();
		//webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.setViewSize(new Dimension(320, 240));
		
		
		webcamPanel = new WebcamPanel(webcam);
		webcamPanel.setImageSizeDisplayed(true);
		panel.add(webcamPanel, "cell 1 3,growx"); //$NON-NLS-1$
		
		
		lblStatus = new JLabel(""); //$NON-NLS-1$
		panel.add(lblStatus, "cell 1 4,alignx trailing"); //$NON-NLS-1$
		
		
		
//		JFrame frame = new JFrame();
//		frame.add(webcamPanel);
//		frame.setLocationRelativeTo(null);
//		frame.pack();
//		frame.setVisible(true);

//		JLabel lblFaceValue = new JLabel(Messages.getString("GiftCertDialog.8")); //$NON-NLS-1$
//		panel.add(lblFaceValue, "cell 0 1,alignx trailing"); //$NON-NLS-1$

//		tfFaceValue = new DoubleTextField();
//		tfFaceValue.setText("50"); //$NON-NLS-1$
//		panel.add(tfFaceValue, "cell 1 1,growx"); //$NON-NLS-1$
//
//		qwertyKeyPad = new QwertyKeyPad();
//		panel.add(qwertyKeyPad, "newline, gaptop 10px, span"); //$NON-NLS-1$
	}
	
	protected void updateLabelsAmount() {
		// TODO Auto-generated method stub
		if(cbPaymentType.getSelectedItem() == PaymentTypeWallet.BALANCE) {
			lblBalance.setText("Balance");
			amount = String.valueOf(tenderAmount);
			lblAmount.setText(amount);
			
		}
		else if(cbPaymentType.getSelectedItem() == PaymentTypeWallet.POINTS){
			lblBalance.setText("Points");
			amount = String.valueOf(pointsAmount);
			lblAmount.setText(amount);
			
		}
//		lblBalance.setText(cbPaymentType.getSelectedItem().toString());
	}

	public String readQR(BufferedImage bufferedImage) {
		String QR = null;
		try {
			LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result result = new MultiFormatReader().decode(bitmap);
			
			QR = result.getText();
			System.out.println("QR reading: " + QR);
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			//System.out.println("There is no QR code in the image");
			e.printStackTrace();
		}
		return QR;
	}

	
	
	@Override
	public void doOk() {
		lblStatus.setText("Reading QR...");
		for(int i = 0; i < 50; i++) {
			QR = readQR(webcam.getImage());
			webcamPanel.repaint();
			if(QR !=null)
				break;
			try {
				Thread.sleep(60);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		if (StringUtils.isEmpty(getQR()) && StringUtils.isEmpty(QR)) {
			POSMessageDialog.showMessage("Not QR readed"); //$NON-NLS-1$
			return;
		}
		
		lblStatus.setText("Transaction Request...");
		if(!postTransaction()) {
			return;
		}
		
		

		setCanceled(false);
		dispose();
	}
	
	@Override
	public void dispose(){
		webcam.close();
		super.dispose();
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
		String dWalletUser;
		if(QR != null) {
			dWalletUser = QR;
		}
		else{
			dWalletUser = getQR();
		}
		
		
		JsonObject json = new JsonObject();
		json.addProperty("description", "FloreantPOS");    
		json.addProperty("amount", amount);    
		json.addProperty("type", cbPaymentType.getSelectedItem().toString());    
		json.addProperty("purchaseDate", LocalDate.now().toString());    
		json.addProperty("qr", dWalletUser);    
		
		System.out.println(json.toString());
		
		String postUrl = "https://dwallet.survivorsofmars.tk/api/v1/transaction/qr";
		try {
			URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(40000);
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
			return false;
		}
		
		return true;
		
	}

}
