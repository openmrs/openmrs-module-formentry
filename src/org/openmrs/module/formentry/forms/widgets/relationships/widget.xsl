
<div align="left"> </div>
<div align="left">
    <strong>
        <font size="3" face="Arial">Relationships:</font>
    </strong>
</div>
<div align="left"> </div>
<div align="left">
    <table style="border-style: none; width: 755px; border-collapse: collapse; word-wrap: break-word; table-layout: fixed;" 
        class=" xdRepeatingTable msoUcTable" 
        title="" 
        border="1" 
        xd:CtrlId="PatientRelationshipTable">
        <colgroup>
            <col style="width: 268px"></col>
            <col style="width: 158px"></col>
            <col style="width: 268px"></col>
            <col style="width: 61px"></col>
        </colgroup>
        <tbody class="xdTableHeader">
            <tr style="min-height: 20px">
                <td style="border: #000000 1pt solid; border-width: 1pt 0 1pt 1pt; padding: 5px; vertical-align: top;">
                    <div>Name</div>
                </td>
                <td style="border: #000000 1pt solid; border-width: 1pt 0; padding: 5px; vertical-align: top;">
                    <div>Identifier</div>
                </td>
                <td style="border: #000000 1pt solid; border-width: 1pt 0; padding: 5px; vertical-align: top;">
                    <div>Relationship</div>
                </td>
                <td style="border: #000000 1pt solid; border-width: 1pt 1pt 1pt 0; padding: 5px; vertical-align: top;">
                    <div align="center"> </div>
                </td>
            </tr>
        </tbody>
        <tbody style="mso-spreadsheet-section: dynamic" xd:xctname="repeatingtable">
            <xsl:for-each select="patient/patient_relationship">
                <xsl:if test="not((patient_relationship.relationship_type_id = &quot;&quot;))">
                    <tr>
                        <td style="border: #000000 1pt solid; border-width: 1pt 0 1pt 1pt; padding: 5px; vertical-align: middle;">
                            <span class="xdExpressionBox xdDataBindingUI" title="" 
                                xd:CtrlId="PatientRelationshipRelative" xd:xctname="ExpressionBox" 
                                xd:binding="concat(relative/relative.given_name, &quot; &quot;, relative/relative.middle_name, &quot; &quot;, relative/relative.family_name)">
                                <xsl:attribute name="style">
                                    overflow-y: hidden; width: 260px; font-family: Arial; word-wrap: normal; white-space: nowrap; height: 22px; font-size: x-small;
                                    <xsl:choose>
                                        <xsl:when test="patient_relationship.voided = 1">color: #808080; text-decoration: line-through</xsl:when>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:value-of select="concat(relative/relative.given_name, &quot; &quot;, relative/relative.middle_name, &quot; &quot;, relative/relative.family_name)"/>
                            </span>
                        </td>
                        <td style="border: #000000 1pt solid; border-width: 1pt 0; padding: 5px; vertical-align: middle;">
                            <span class="xdExpressionBox xdDataBindingUI" title="" 
                                xd:CtrlId="PatientRelationshipRelative" xd:xctname="ExpressionBox" 
                                xd:binding="concat(&quot;&quot;, relative/relative.identifier)">
                                <xsl:attribute name="style">
                                    overflow-y: hidden; width: 150px; font-family: Arial; word-wrap: normal; white-space: nowrap; height: 22px; font-size: x-small;
                                    <xsl:choose>
                                        <xsl:when test="patient_relationship.voided = 1">color: #808080; text-decoration: line-through</xsl:when>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:value-of select="concat(&quot;&quot;,relative/relative.identifier)"/>
                            </span>
                        </td>
                        <td style="border: #000000 1pt solid; border-width: 1pt 0; padding: 5px; vertical-align: middle;">
                            <span class="xdExpressionBox xdDataBindingUI" title="" 
                                xd:CtrlId="PatientRelationshipDescription" xd:xctname="ExpressionBox" 
                                xd:binding="concat(patient_relationship.description, &quot; (&quot;, ../patient.given_name, &quot; is &quot;, patient_relationship.reverse_description, &quot;)&quot;)">
                                <xsl:attribute name="style">
                                    overflow-y: hidden; width: 260px; font-family: Arial; word-wrap: normal; white-space: nowrap; height: 22px; font-size: x-small;<xsl:choose>
                                        <xsl:when test="patient_relationship.voided = 1">color: #808080; text-decoration: line-through</xsl:when>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:value-of select="concat(patient_relationship.description, &quot; (&quot;, ../patient.given_name, &quot; is &quot;, patient_relationship.reverse_description, &quot;)&quot;)"/>
                            </span>
                        </td>
                        <td style="border: #000000 1pt solid; border-width: 1pt 1pt 1pt 0; padding: 5px; vertical-align: middle;">
                            <div align="center">
                                <input style="font-family: Arial; font-size: xx-small" class="langFont" title="" value="Delete" type="button" xd:CtrlId="deleteNewRelationship" xd:xctname="Button">
                                    <xsl:attribute name="style">font-family: Arial; font-size: xx-small;<xsl:choose>
                                        <xsl:when test="patient_relationship.exists = 1">display: none</xsl:when>
                                    </xsl:choose>
                                    </xsl:attribute>
                                </input>
                            </div>
                        </td>
                    </tr>
                </xsl:if>
            </xsl:for-each>
        </tbody>
        <tbody class="xdTableFooter">
            <tr style="min-height: 20px">
                <td colSpan="4" style="border: #000000 1pt solid; padding: 5px; vertical-align: middle;">
                    <div align="center">
                        <input style="font-family: Arial; font-size: xx-small" class="langFont" title="" value="Add New Relationship" type="button" xd:CtrlId="addRelationship" xd:xctname="Button"/>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
</div>
