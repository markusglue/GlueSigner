package androidGLUESigner.interfaces;

/**
 * Callbacks for various GUI events during the signing process
 * @author roland
 *
 */
public interface IWizardEventListener {
	
	/**
	 * is called when the document has been successfully signed
	 * @param outputPath the path of the signed document
	 */
	public void SignatureComplete(String outputPath);
	
	/**
	 * updates the default signature information
	 */
	public void updateDefaultSettings();

	/**
	 * enables or disables the next button
	 * @param enabled true if enabling
	 */
	public void setNextButtonEnabled(boolean enabled);

	/**
	 * prevents the next step from being loaded
	 */
	public void setStepCancelled();
}
