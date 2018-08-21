package com.floreantpos.extension;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import com.floreantpos.config.ui.ConfigurationView;
import com.floreantpos.config.ui.DWalletMerchantGatewayConfigurationView;
import com.floreantpos.config.ui.DefaultMerchantGatewayConfigurationView;
import com.floreantpos.config.ui.InginicoConfigurationView;
import com.floreantpos.model.PosTransaction;
import com.floreantpos.model.Ticket;
import com.floreantpos.ui.views.payment.CardProcessor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class DWalletGatewayPlugin  extends PaymentGatewayPlugin{
	DWalletMerchantGatewayConfigurationView view;

	@Override
	public String getProductName() {
		return "DWallet"; //$NON-NLS-1$
	}

	@Override
	public void initUI() {
	}

	@Override
	public void initBackoffice() {
	}

	@Override
	public void initConfigurationView(JDialog dialog) {

	}

	@Override
	public String getId() {
		return "Authorize.Net"; // //$NON-NLS-1$
	}

	@Override
	public String getSecurityCode() {
		return "-1956568219";//$NON-NLS-1$
	}

	@Override
	public String toString() {
		return getProductName();
	}

	@Override
	public ConfigurationView getConfigurationPane() throws Exception {
		if (view == null) {
			view = new DWalletMerchantGatewayConfigurationView();
			view.initialize();
		}

		return view;
	}

	@Override
	public CardProcessor getProcessor() {
		return null;
	}

	@Override
	public boolean shouldShowCardInputProcessor() {
		return true;
	}

	@Override
	public List<AbstractAction> getSpecialFunctionActions() {
		return null;
	}

	@Override
	public void initLicense() {
	}

	@Override
	public boolean hasValidLicense() {
		return true;
	}

	@Override
	public String getProductVersion() {
		return null;
	}

	@Override
	public Component getParent() {
		return null;
	}

	@Override
	public boolean printUsingThisTerminal() {
		return false;
	}

	@Override
	public void printTicket(Ticket ticket) {
	}

	@Override
	public void printTransaction(PosTransaction transaction, boolean storeCopy, boolean customerCopy) {

	}

	@Override
	public void printTicketWithTipsBlock(Ticket ticket) {
	}

}
