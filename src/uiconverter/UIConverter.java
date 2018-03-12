/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uiconverter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 *
 * @author ravindu
 */
public class UIConverter {

    private int template = 1;
    private String html = "";
    private boolean divStart = false;
    private boolean rowStart = false;
    private boolean paraStart = false;
    private boolean startTable = false;
    private boolean startTree = false;
    private String lastTag = "";
    private int currentTableColumnCount = 1;

    private void addHtml(String line) {
        if (startTable) {
            return;
        }
        if (!paraStart) {
            startPara();
        }
        html += line + " ";
    }

    private void addHtml(String tag, String line) {
        addHtml(tag, line, true);
    }

    private void addHtml(String tag, String line, boolean closeDiv) {
        if (startTable) {
            closeTable();
        }
        String styleClass = "";
        if (tag.equals("div")) {
            if (paraStart) {
                closePara();
            }
            if (rowStart) {
                styleClass = "row";
            } else {
                styleClass = "cols-md-" + (12 / template);
            }
        }
        if (tag.equals("h1") || tag.equals("h2") || tag.equals("h3") || tag.equals("h4")) {
            if (paraStart) {
                closePara();
            }
            if (!divStart) {
                html += "<" + tag + " style=\"text-align:center\" >" + line + "</" + tag + ">";
            } else {
                if (lastTag.equals("content")) {
                    html += "<br/><br/>";
                }
                html += "<" + tag + " >" + line + "</" + tag + ">";
            }
        } else {
            if (lastTag.equals("h1") || lastTag.equals("h2") || lastTag.equals("h3") || lastTag.equals("h4")) {
                html += "<br/><br/>";
            }
            html += "<" + tag + " class=\"" + styleClass + "\" >" + line + "</" + tag + ">";
        }
        if (!divStart) {
            html += "<br/>";
        }
        lastTag = tag;
    }

    private void addImage(String image, boolean divImage, String style) {
        if (paraStart) {
            closePara();
        }
        if (startTable) {
            closeTable();
        }
        html += "<br/><br/>";
        html += "<img src=\"" + image + "\" style=\"";
        if (divImage) {
            html += "float:left;";
        }
        if (style != null) {
            html += style;
        } else {
            html += "width:100%;";
        }

        html += "\" />";
        html += "<br/><br/>";
    }

    private boolean startPara() {
        if (startTable) {
            closeTable();
        }
        if (divStart && !paraStart) {
            paraStart = true;
            html += "<p>";
            return false;
        }
        return true;
    }

    private void closePara() {
        if (paraStart) {
            paraStart = false;
            html += "</p>";
        }
    }

    private void makeTable(String tag) {
        makeTable(tag, null);
    }

    private void startTable() {
        makeTable("table", null);
    }

    private void closeTable() {
        makeTable("end", null);
    }

    private void makeTable(String tag, String line) {
        if (tag.equals("table")) {
            closePara();
            html += "<table class=\"table table-bordered table-sm table-hover table-striped\">\n";
            startTable = true;
        } else if (tag.equals("th")) {
            html += "<tr>";
            String[] cells = line.split(",");
            for (String cell : cells) {
                html += "<th>" + cell.trim() + "</th>";
            }
            html += "</tr>\n";
            currentTableColumnCount = cells.length;
        } else if (tag.equals("td")) {
            html += "<tr>";
            String[] cells = line.split(",");
            for (String cell : cells) {
                html += "<td>" + cell.trim() + "</td>";
            }
            html += "</tr>\n";
        } else if (tag.equals("group")) {
            html += "<tr>";
            html += "<th colspan=\"" + currentTableColumnCount + "\" >" + line.trim() + "</th>";
            html += "</tr>\n";
        } else if (tag.equals("end")) {
            if (startTable) {
                html += "</table>\n";
                startTable = false;
                currentTableColumnCount = 1;
            }
        }

    }

    public String convert(List<String> lines) throws IOException {        
        for (String line : lines) {
            if (line.startsWith("#template")) {
                template = Integer.parseInt(line.replace("#template", "").trim());
            } else if (line.startsWith("#section")) {
                closePara();
                if (divStart) {
                    divStart = false;
                    html += "</div>";
                }
                if (!rowStart) {
                    rowStart = true;
                    html += "<div class=\"row\" >";
                }
                html += "<div class=\"col-md-" + (12 / template) + "\" >";
                divStart = true;
                lastTag = "div";
                String sectionBody = line.replace("#section", "").trim();
                if (!sectionBody.isEmpty()) {
                    addImage(sectionBody, true, "width:40%;margin-left:10 px;");
                }

            } else if (line.startsWith("#image ")) {
                addImage(line.substring(6), false, null);
                //table syntax ///////////////////////////////////
            } else if (line.startsWith("#table")) {
                startTable();
            } else if (line.startsWith("#titles")) {
                makeTable("th", line.substring(7));
            } else if (line.startsWith("#group")) {
                makeTable("group", line.substring(7));
            } else if (line.startsWith("-")) {
                makeTable("td", line.substring(1));
                //end table syntax /////////////////////////////////
            } else if (line.startsWith("####")) {
                addHtml("h4", line.substring(4));
            } else if (line.startsWith("###")) {
                addHtml("h3", line.substring(3));
            } else if (line.startsWith("##")) {
                addHtml("h2", line.substring(2));
            } else if (line.startsWith("#")) {
                addHtml("h1", line.substring(1));
            } else if (line.trim().isEmpty()) {
                if (startPara()) {
                    closePara();
                }
                if (startTable) {
                    closeTable();
                }
            } else {
                lastTag = "content";
                addHtml(line);
            }
            addHtml("\n");
        }
        if (paraStart) {
            closePara();
        }
        if (divStart) {
            divStart = false;
            html += "</div>";
        }
        if (rowStart) {
            rowStart = false;
            html += "</div>";
        }
        
        html = html.replace("<p></p>", "");
        html = html.replace("<p> </p>", "");
        html = html.replaceAll("<p>\n *</p>", "");
        System.out.println(html);
        html = UITemplate.header + html + UITemplate.footer;
        return html;
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("example1.txt"), Charset.defaultCharset());
        String html = new UIConverter().convert(lines);        
        Files.write(Paths.get("/home/ravindu/Desktop/a.html"), html.getBytes(), StandardOpenOption.CREATE);
    }

}
