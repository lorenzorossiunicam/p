package it.unicam.pros.purple.gui.ui.view;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.unicam.pros.purple.gui.ui.components.FlexBoxLayout;

/**
 * A view frame that establishes app design guidelines. It consists of three
 * parts:
 * <ul>
 * <li>Topmost {@link #setViewHeader(Component...) header}</li>
 * <li>Center {@link #setViewContent(Component...) content}</li>
 * <li>Bottom {@link #setViewFooter(Component...) footer}</li>
 * </ul>
 */
@CssImport("./styles/components/view-frame.css")
public class ViewFrame extends Composite<Div> implements HasStyle {

	private final Dialog error;
	private final Text errorText;
	private String CLASS_NAME = "view-frame";

	private Div header;
	private Div content;
	private Div footer;

	public ViewFrame() {
		setClassName(CLASS_NAME);

		header = new Div();
		header.setClassName(CLASS_NAME + "__header");

		content = new Div();
		content.setClassName(CLASS_NAME + "__content");

		footer = new Div();
		footer.setClassName(CLASS_NAME + "__footer");

		errorText = new Text("");
		error = new Dialog(new FlexBoxLayout(new VerticalLayout(new H4("Error"), errorText)));
		error.setWidth("70%");
		error.setHeight("35%");

		getContent().add(header, content, footer);
	}

	public void showError(String e){
		errorText.setText(e);
		error.open();
	}
	/**
	 * Sets the header slot's components.
	 */
	public void setViewHeader(Component... components) {
		header.removeAll();
		header.add(components);
	}

	/**
	 * Sets the content slot's components.
	 */
	public void setViewContent(Component... components) {
		content.removeAll();
		content.add(components);
	}

	/**
	 * Sets the footer slot's components.
	 */
	public void setViewFooter(Component... components) {
		footer.removeAll();
		footer.add(components);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		MainLayout.get().getAppBar().reset();
	}

}
