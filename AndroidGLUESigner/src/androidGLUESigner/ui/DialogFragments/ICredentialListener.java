/**
 * 
 */
package androidGLUESigner.ui.DialogFragments;

/**
 * @author roland
 * callback as recommended by android dev guide for comm between dialog and fragment/activity
 */
public interface ICredentialListener {
	/**
	 * set the password/pin
	 * @param credential the password / pin
	 */
	public void setCredential(String credential);

}
