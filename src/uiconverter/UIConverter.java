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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author ravindu
 */
public class UIConverter {

    private int template = 1;
    private boolean template3 = false;
    private boolean section3Started = false;
    private int currentSection = 0;
    private String html = "";
    private boolean divStart = false;
    private boolean rowStart = false;
    private boolean paraStart = false;
    private boolean startTable = false;
    private boolean startTree = false;
    private String lastTag = "";
    private int currentTableColumnCount = 1;

    private String contextpath;
    private String imagePath;

    public UIConverter(String contextpath, String imagePath) {
//        this.contextpath = contextpath;
//        this.imagePath = contextpath + "/resources/images/albums/" + imagePath + "/";
        this.contextpath = "";
        this.imagePath = imagePath;
    }

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
            String align = null;
            Pattern pattern = Pattern.compile("align[(](.*?)[)]");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                align = matcher.group(1);
                line = line.replace("align(" + align + ")", "");
            }
            if (paraStart) {
                closePara();
            }
            if (!divStart) {
                if (align == null) {
                    align = "center";
                }
                html += "<" + tag + " style=\"text-align:" + align +"\" >" + line + "</" + tag + ">";
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

    private void addImage(String image, boolean divImage, String style, Boolean spacesBefore, Boolean spacesAfter) {
        String width = null;

        Pattern pattern = Pattern.compile("width[(](.*?)[)]");
        Matcher matcher = pattern.matcher(image);
        if (matcher.find()) {
            width = matcher.group(1);
            image = image.replace("width(" + width + ")", "");
        }
        if (paraStart) {
            closePara();
        }
        if (startTable) {
            closeTable();
        }
        if (spacesBefore) {
            html += "<br/><br/>";
        }
        html += "<img src=\"" + imagePath + image + "\" style=\"";
        if (divImage) {
            html += "float:left;";
        }
        if (style != null) {
            html += style;
        } else {
            if (width == null) {
                html += "width:100%;";
            } else {
                html += "width:" + width + "%;";
            }
        }

        html += "\" />";
        if (spacesAfter) {
            html += "<br/><br/>";
        }
    }

    private void addVideo(String image, String style, Boolean spacesBefore, Boolean spacesAfter) {
        if (paraStart) {
            closePara();
        }
        if (startTable) {
            closeTable();
        }
        if (spacesBefore) {
            html += "<br/><br/>";
        }
        html += "<video controls=\"true\" src=\"" + imagePath + image + "\" style=\"";

        if (style != null) {
            html += style;
        } else {
            html += "width:100%;";
        }

        html += "\" />";
        if (spacesAfter) {
            html += "<br/><br/>";
        }
    }

    private void addFile(String image, String style, Boolean spacesBefore, Boolean spacesAfter) {
        if (paraStart) {
            closePara();
        }
        if (startTable) {
            closeTable();
        }
        if (spacesBefore) {
            html += "<br/><br/>";
        }
        html += "<a href=\"" + imagePath + image + "\" >"
                + "<img src=\"" + contextpath + "/resources/images/albums/file.png\" style=\"width: 30px;\" />"
                + image.replaceAll(".*/", "")
                + "</a><img  style=\"";
        if (style != null) {
            html += style;
        } else {
            html += "width:100%;";
        }

        html += "\" />";
        html += "<br/>";
        if (spacesAfter) {
            html += "<br/><br/>";
        }
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

    public String convert(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith("#template")) {
                template = Integer.parseInt(line.replace("#template", "").trim());
                if (template == 3) {
                    template = 2;
                    template3 = true;
                }
            } else if (line.startsWith("#section")) {
                currentSection++;
                closePara();
                if (divStart) {
                    divStart = false;
                    html += "</div>";
                }
                if (currentSection == 3 && !section3Started) {
                    section3Started = true;
                    html += "</div>";
                    html += "<div class=\"row\" >";
                    html += "<div class=\"col-md-12\" >";
                    divStart = true;
                    lastTag = "div";
                } else {
                    if (!rowStart) {
                        rowStart = true;
                        html += "<div class=\"row\" >";
                    }
                    html += "<div class=\"col-md-" + (12 / template) + "\" >";
                    divStart = true;
                    lastTag = "div";
                    String sectionBody = line.replace("#section", "").trim();
                    if (!sectionBody.isEmpty()) {
                        addImage(sectionBody.replace("image:", ""), true, "width:40%;margin-right:10px;", false, false);
                    }
                }
            } else if (line.startsWith("image:")) {
                addImage(line.substring(6), false, null, true, true);
            } else if (line.startsWith("video:")) {
                addVideo(line.substring(6), null, true, true);
            } else if (line.startsWith("file:")) {
                addFile(line.substring(6), null, false, false);
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
            } else if (line.startsWith("#######")) {
                addHtml("h7", line.substring(7));
            } else if (line.startsWith("#######")) {
                addHtml("h6", line.substring(6));
            } else if (line.startsWith("#####")) {
                addHtml("h5", line.substring(5));
            } else if (line.startsWith("####")) {
                addHtml("h4", line.substring(4));
            } else if (line.startsWith("###")) {
                addHtml("h3", line.substring(3));
            } else if (line.startsWith("##")) {
                addHtml("h2", line.substring(2));
            } else if (line.startsWith("#")) {
                addHtml("h1", line.substring(1));
            } else if (line.startsWith("___#")) {
                continue;
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
        if (startTable) {
            closeTable();
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

    public String convert(String[] lines) {
        return convert(Arrays.asList(lines));
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("example1.txt"), Charset.defaultCharset());
        String html = new UIConverter("", "").convert(lines);
        Files.write(Paths.get("/home/ravindu/Desktop/a.html"), html.getBytes(), StandardOpenOption.CREATE);
    }

}
