package com.jayway.jersey.rest.constraint;

import com.jayway.jersey.rest.resource.ContextMap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(RequiresRoles.Evaluator.class)
public @interface RequiresRoles {

    Class<?>[] value();

    class Evaluator implements ConstraintEvaluator<RequiresRoles, ContextMap>{

        public boolean isValid( RequiresRoles role, ContextMap map ) {
            for ( Class<?> clazz : role.value() ) {
                if ( map.get( clazz ) == null ) return false;
            }
            return true;
        }

    }

}
