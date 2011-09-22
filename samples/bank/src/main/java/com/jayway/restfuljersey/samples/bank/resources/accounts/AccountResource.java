package com.jayway.restfuljersey.samples.bank.resources.accounts;

import static com.jayway.jersey.rest.resource.ResourceUtil.role;

import com.jayway.jersey.rest.dto.BooleanDTO;
import com.jayway.jersey.rest.dto.IntegerDTO;
import com.jayway.jersey.rest.resource.IndexResource;
import com.jayway.restfuljersey.samples.bank.constraints.DepositAllowed;
import com.jayway.restfuljersey.samples.bank.constraints.HasCredit;
import com.jayway.restfuljersey.samples.bank.constraints.HasRole;
import com.jayway.restfuljersey.samples.bank.dto.TransferToDTO;
import com.jayway.restfuljersey.samples.bank.model.Account;
import com.jayway.restfuljersey.samples.bank.model.AccountManager;
import com.jayway.restfuljersey.samples.bank.model.Depositable;
import com.jayway.restfuljersey.samples.bank.model.Withdrawable;
import com.jayway.restfuljersey.samples.bank.repository.AccountRepository;

/**
 */
public class AccountResource implements IndexResource {

    @Override
    public Object index() {
        Account account = role(Account.class);
        return String.format( "<html><h1>Account: %s</h1> Balance = %d <br>Account is allowed to exceed deposit ensured limit: %s</html>", account.getAccountNumber(), account.getBalance(), account.isAllowExceedBalanceLimit() );
    }

    public void allowexceeddepositlimit( BooleanDTO allow ) {
        role(Account.class).setAllowExceedBalanceLimit(allow.bool());
    }

    @DepositAllowed
    public void deposit( IntegerDTO amount ) {
        role(AccountManager.class).deposit((Depositable) role(Account.class), amount.getInteger());
    }

    @HasCredit
    @HasRole( object = Account.class, hasRole = Withdrawable.class )
    public void withdraw( IntegerDTO amount ) {
        role(AccountManager.class).withdraw((Withdrawable) role(Account.class), amount.getInteger());
    }

    @HasCredit
    @HasRole( object = Account.class, hasRole = Withdrawable.class)
    public void transfer( TransferToDTO transfer ) {
        Depositable depositable = role(AccountRepository.class).findWithRole(transfer.getDestinationAccount(), Depositable.class);
        Withdrawable withdrawable = (Withdrawable) role(Account.class);

        role( AccountManager.class ).transfer(withdrawable, depositable, transfer.getAmount() );
    }

}
