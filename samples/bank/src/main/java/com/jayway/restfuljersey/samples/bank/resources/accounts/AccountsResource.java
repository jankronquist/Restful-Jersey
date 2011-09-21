package com.jayway.restfuljersey.samples.bank.resources.accounts;

import static com.jayway.jersey.rest.resource.ResourceUtil.addRole;
import static com.jayway.jersey.rest.resource.ResourceUtil.role;

import com.jayway.jersey.rest.dto.StringDTO;
import com.jayway.jersey.rest.resource.IdResource;
import com.jayway.jersey.rest.resource.IndexResource;
import com.jayway.jersey.rest.resource.Resource;
import com.jayway.restfuljersey.samples.bank.helper.HtmlHelper;
import com.jayway.restfuljersey.samples.bank.model.Account;
import com.jayway.restfuljersey.samples.bank.repository.AccountRepository;

/**
 */
public class AccountsResource implements IndexResource, IdResource {
	
    @Override
    public Object index() {
        return HtmlHelper.toLinks(role(AccountRepository.class).all());
    }

    @Override
    public Resource id(StringDTO id) {
        Account account = role(AccountRepository.class).findById(id.string());
        addRole( Account.class, account);
        return new AccountResource();
    }

}
