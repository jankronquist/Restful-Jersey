package com.jayway.restfuljersey.samples.bank.constraints;

import com.jayway.jersey.rest.constraint.Constraint;
import com.jayway.jersey.rest.constraint.ConstraintEvaluator;
import com.jayway.jersey.rest.resource.ContextMap;
import com.jayway.restfuljersey.samples.bank.model.Account;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(DepositAllowed.Evaluator.class)
public @interface DepositAllowed {

    class Evaluator implements ConstraintEvaluator<DepositAllowed, ContextMap> {

        public boolean isValid( DepositAllowed role, ContextMap map ) {
            Account account = map.get(Account.class);
            if ( account == null ) return false;

            if ( account.getBalance() >= Account.MAX_ENSURED_BALANCE ) {
                return account.isAllowExceedBalanceLimit();
            } else {
                return true;
            }
        }

    }

}
