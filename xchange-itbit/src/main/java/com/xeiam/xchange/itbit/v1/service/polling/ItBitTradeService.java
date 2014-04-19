/**
 * Copyright (C) 2012 - 2014 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.itbit.v1.service.polling;

import java.io.IOException;
import java.util.Date;

import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.NotAvailableFromExchangeException;
import com.xeiam.xchange.NotYetImplementedForExchangeException;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.itbit.v1.ItBitAdapters;
import com.xeiam.xchange.itbit.v1.dto.trade.ItBitOrder;
import com.xeiam.xchange.itbit.v1.dto.trade.ItBitPlaceOrderRequest;
import com.xeiam.xchange.service.polling.PollingTradeService;

public class ItBitTradeService extends ItBitBasePollingService implements PollingTradeService {
	/** Wallet ID used for transactions with this instance */
	private final String walletId;

	/**
	 * Constructor
	 * 
	 * @param exchangeSpecification
	 *          The {@link ExchangeSpecification}
	 */
	public ItBitTradeService(ExchangeSpecification exchangeSpecification) {
		super(exchangeSpecification);
		
		// wallet Id used for this instance.
		walletId = (String)exchangeSpecification.getExchangeSpecificParameters().get("walletId");
	}

	@Override
	public OpenOrders getOpenOrders() throws ExchangeException,
	NotAvailableFromExchangeException,
	NotYetImplementedForExchangeException, IOException {		
		ItBitOrder[] orders = itBit.getOrders(signatureCreator, new Date().getTime(), nextNonce(), "XBTUSD", "1", "1000", "open", walletId);

		return ItBitAdapters.adaptPrivateOrders(orders);
	}

	@Override
	public String placeMarketOrder(MarketOrder marketOrder)
			throws ExchangeException, NotAvailableFromExchangeException,
			NotYetImplementedForExchangeException, IOException {
		throw new NotYetImplementedForExchangeException();
	}

	@Override
	public String placeLimitOrder(LimitOrder limitOrder) throws ExchangeException,
	NotAvailableFromExchangeException,
	NotYetImplementedForExchangeException, IOException {

		String side = limitOrder.getType().equals(OrderType.BID) ? "buy" : "sell";

		ItBitOrder postOrder = itBit.postOrder(signatureCreator, new Date().getTime(), nextNonce(), walletId, 
				new ItBitPlaceOrderRequest(
						side, 
						"limit", 
						limitOrder.getCurrencyPair().baseSymbol, 
						limitOrder.getTradableAmount(), 
						limitOrder.getLimitPrice(), 
						limitOrder.getCurrencyPair().baseSymbol + limitOrder.getCurrencyPair().counterSymbol));

		return postOrder.getId();
	}

	@Override
	public boolean cancelOrder(String orderId) throws ExchangeException,
	NotAvailableFromExchangeException,
	NotYetImplementedForExchangeException, IOException {
		itBit.cancelOrder(signatureCreator, new Date().getTime(), nextNonce(), walletId, orderId);
		return true;
	}

	@Override
	public Trades getTradeHistory(Object... arguments) throws ExchangeException,
	NotAvailableFromExchangeException,
	NotYetImplementedForExchangeException, IOException {		
		String currency = null;

		if(arguments.length == 1) {
			CurrencyPair currencyPair = ((CurrencyPair) arguments[0]);
			currency = currencyPair.baseSymbol + currencyPair.counterSymbol;
		} else {
			currency = "XBTUSD";
		}

		ItBitOrder[] orders = itBit.getOrders(signatureCreator, new Date().getTime(), nextNonce(), currency, "1", "1000", "filled", walletId);		
		return ItBitAdapters.adaptTradeHistory(orders);
	}
}
