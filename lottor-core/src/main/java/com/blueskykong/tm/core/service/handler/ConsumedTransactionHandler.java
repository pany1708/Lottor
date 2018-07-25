package com.blueskykong.tm.core.service.handler;

import com.blueskykong.tm.common.bean.TxTransactionInfo;
import com.blueskykong.tm.common.entity.TransactionMsg;
import com.blueskykong.tm.common.enums.ConsumedStatus;
import com.blueskykong.tm.common.holder.LogUtil;
import com.blueskykong.tm.core.compensation.TxOperateService;
import com.blueskykong.tm.core.service.TxManagerMessageService;
import com.blueskykong.tm.core.service.TxTransactionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ConsumedTransactionHandler implements TxTransactionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumedTransactionHandler.class);

    private final TxManagerMessageService txManagerMessageService;

    private final TxOperateService txOperateService;

    public ConsumedTransactionHandler(TxManagerMessageService txManagerMessageService, TxOperateService txOperateService) {
        this.txManagerMessageService = txManagerMessageService;
        this.txOperateService = txOperateService;
    }

    @Override
    public void handler(TxTransactionInfo info) {
        try {
            int status;
            if (info.getArgs().length != 2) {
                throw new IllegalArgumentException("wrong arguments for consuming!");
            }
            if ((Boolean) info.getArgs()[1] == true) {
                status = ConsumedStatus.CONSUMED_SUCCESS.getStatus();
            } else {
                status = ConsumedStatus.CONSUMED_FAILURE.getStatus();
            }
            LogUtil.debug(LOGGER, "consume status: {}", () -> status);
            TransactionMsg transactionMsg = (TransactionMsg) info.getArgs()[0];
            transactionMsg.setConsumed(status);
            transactionMsg.setUpdateTime(System.currentTimeMillis());
            // 通知tm完成事务消息消费
            CompletableFuture.runAsync(() -> txManagerMessageService.asyncCompleteConsume(transactionMsg));
            // 完成消费为异步，本地记录结果
            txOperateService.saveTransactionMsg(transactionMsg);

            LogUtil.info(LOGGER, "tx-transaction 消费完成, 事务组 id 为 {},消息 id 为：{}",
                    transactionMsg::getGroupId, transactionMsg::getSubTaskId);
        } catch (final Throwable throwable) {
            LogUtil.error(LOGGER, throwable::getLocalizedMessage);
            throw throwable;
        }
    }
}
