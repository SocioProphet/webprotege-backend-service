
package edu.stanford.protege.webprotege.app;

import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.webprotege.authorization.api.ActionId;
import edu.stanford.protege.webprotege.user.UserDetails;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class UserInSession_TestCase {

    private UserInSession userInSession;

    @Mock
    private UserDetails userDetails;

    private Set<ActionId> allowedActions = ImmutableSet.of(new ActionId("TheAction"));

    @Before
    public void setUp() {
        userInSession = new UserInSession(userDetails, allowedActions);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = java.lang.NullPointerException.class)
    public void shouldThrowNullPointerExceptionIf_userDetails_IsNull() {
        new UserInSession(null, allowedActions);
    }

    @Test
    public void shouldReturnSupplied_userDetails() {
        assertThat(userInSession.getUserDetails(), is(this.userDetails));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = java.lang.NullPointerException.class)
    public void shouldThrowNullPointerExceptionIf_allowedActions_IsNull() {
        new UserInSession(userDetails, null);
    }

    @Test
    public void shouldReturnSupplied_allowedActions() {
        assertThat(userInSession.getAllowedApplicationActions(), is(this.allowedActions));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(userInSession, is(userInSession));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(userInSession.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(userInSession, is(new UserInSession(userDetails, allowedActions)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_userDetails() {
        assertThat(userInSession, is(not(new UserInSession(mock(UserDetails.class), allowedActions))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_allowedActions() {
        assertThat(userInSession, is(not(new UserInSession(userDetails, ImmutableSet.of(new ActionId("OtherAction"))))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(userInSession.hashCode(), is(new UserInSession(userDetails, allowedActions).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(userInSession.toString(), Matchers.startsWith("UserInSession"));
    }

}
