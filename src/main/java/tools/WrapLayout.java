/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

/**
 *
 * @author ronal
 */
import java.awt.*;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class WrapLayout extends FlowLayout {

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target);
    }

    private Dimension layoutSize(Container target) {

        synchronized (target.getTreeLock()) {

            int targetWidth = target.getWidth();
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            for (Component comp : target.getComponents()) {
                if (!comp.isVisible()) {
                    continue;
                }

                Dimension d = comp.getPreferredSize();

                if (rowWidth + d.width > maxWidth) {
                    dim.width = Math.max(dim.width, rowWidth);
                    dim.height += rowHeight + vgap;
                    rowWidth = 0;
                    rowHeight = 0;
                }

                if (rowWidth != 0) {
                    rowWidth += hgap;
                }

                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight + vgap;

            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }
    }
}
