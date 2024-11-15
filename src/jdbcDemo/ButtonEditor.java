package jdbcDemo;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String label;
    private boolean isPushed;
    private boolean discountApplied; // Flag to track if the discount has been applied

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        discountApplied = false; // Initialize flag to false
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!discountApplied) { 
                    applyDiscount(); 
                    discountApplied = true; 
                    button.setEnabled(false); 
                    fireEditingStopped();
                }
            }
        });
    }

    private void applyDiscount() {
        System.out.println("Discount applied to product!");
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        label = (value != null) ? value.toString() : "Apply";
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
