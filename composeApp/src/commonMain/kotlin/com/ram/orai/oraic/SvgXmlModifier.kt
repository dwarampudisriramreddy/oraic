package com.ram.orai.oraic

/**
 * Modifies SVG XML content based on DentalState using simple string replacement
 * This is simpler than XML parsing and works across all platforms
 */
fun modifySvgXml(svgContent: String, state: DentalState?): String {
    if (state == null) return svgContent
    
    var modified = svgContent
    
    // Add viewBox and preserveAspectRatio to SVG if it doesn't have one (for proper scaling and centering)
    // Also scale down the SVG for JVM (90% scale)
    val scaleFactor = 0.9
    if (!modified.contains("viewBox=")) {
        // Extract width and height from SVG
        val widthMatch = Regex("""width\s*=\s*["']([^"']+)["']""").find(modified)
        val heightMatch = Regex("""height\s*=\s*["']([^"']+)["']""").find(modified)
        if (widthMatch != null && heightMatch != null) {
            val originalWidth = widthMatch.groupValues[1]
            val originalHeight = heightMatch.groupValues[1]
            
            // Calculate scaled dimensions
            val widthValue = try {
                originalWidth.toDouble() * scaleFactor
            } catch (e: NumberFormatException) {
                originalWidth.toDoubleOrNull() ?: 0.0
            }
            val heightValue = try {
                originalHeight.toDouble() * scaleFactor
            } catch (e: NumberFormatException) {
                originalHeight.toDoubleOrNull() ?: 0.0
            }
            
            val scaledWidth = if (widthValue > 0) widthValue.toString() else originalWidth
            val scaledHeight = if (heightValue > 0) heightValue.toString() else originalHeight
            
            val viewBox = "viewBox=\"0 0 $originalWidth $originalHeight\""
            val preserveAspectRatio = "preserveAspectRatio=\"xMidYMid meet\""
            
            // Insert viewBox, preserveAspectRatio, and scaled width/height in the opening <svg tag
            modified = Regex("""<svg([^>]*)>""").replace(modified) { matchResult ->
                var attrs = matchResult.groupValues[1]
                if (!attrs.contains("viewBox=")) {
                    attrs = "$viewBox $preserveAspectRatio$attrs"
                }
                // Update width and height to scaled values
                attrs = attrs.replace(Regex("""width\s*=\s*["'][^"']+["']"""), "width=\"$scaledWidth\"")
                attrs = attrs.replace(Regex("""height\s*=\s*["'][^"']+["']"""), "height=\"$scaledHeight\"")
                "<svg $attrs>"
            }
        }
    } else if (!modified.contains("preserveAspectRatio=")) {
        // Add preserveAspectRatio if viewBox exists but preserveAspectRatio doesn't
        // Also scale down width and height
        val widthMatch = Regex("""width\s*=\s*["']([^"']+)["']""").find(modified)
        val heightMatch = Regex("""height\s*=\s*["']([^"']+)["']""").find(modified)
        if (widthMatch != null && heightMatch != null) {
            val originalWidth = widthMatch.groupValues[1]
            val originalHeight = heightMatch.groupValues[1]
            
            val widthValue = try {
                originalWidth.toDouble() * scaleFactor
            } catch (e: NumberFormatException) {
                originalWidth.toDoubleOrNull() ?: 0.0
            }
            val heightValue = try {
                originalHeight.toDouble() * scaleFactor
            } catch (e: NumberFormatException) {
                originalHeight.toDoubleOrNull() ?: 0.0
            }
            
            val scaledWidth = if (widthValue > 0) widthValue.toString() else originalWidth
            val scaledHeight = if (heightValue > 0) heightValue.toString() else originalHeight
            
            modified = Regex("""<svg([^>]*)>""").replace(modified) { matchResult ->
                var attrs = matchResult.groupValues[1]
                if (!attrs.contains("preserveAspectRatio=")) {
                    attrs = "$attrs preserveAspectRatio=\"xMidYMid meet\""
                }
                // Update width and height to scaled values
                attrs = attrs.replace(Regex("""width\s*=\s*["'][^"']+["']"""), "width=\"$scaledWidth\"")
                attrs = attrs.replace(Regex("""height\s*=\s*["'][^"']+["']"""), "height=\"$scaledHeight\"")
                "<svg $attrs>"
            }
        } else {
            // Just add preserveAspectRatio if we can't find width/height
            modified = Regex("""<svg([^>]*)>""").replace(modified) { matchResult ->
                val attrs = matchResult.groupValues[1]
                if (!attrs.contains("preserveAspectRatio=")) {
                    "<svg $attrs preserveAspectRatio=\"xMidYMid meet\">"
                } else {
                    matchResult.value
                }
            }
        }
    } else {
        // Scale down existing width and height even if viewBox and preserveAspectRatio exist
        val widthMatch = Regex("""width\s*=\s*["']([^"']+)["']""").find(modified)
        val heightMatch = Regex("""height\s*=\s*["']([^"']+)["']""").find(modified)
        if (widthMatch != null && heightMatch != null) {
            val originalWidth = widthMatch.groupValues[1]
            val originalHeight = heightMatch.groupValues[1]
            
            val widthValue = try {
                originalWidth.toDouble() * scaleFactor
            } catch (e: NumberFormatException) {
                originalWidth.toDoubleOrNull() ?: 0.0
            }
            val heightValue = try {
                originalHeight.toDouble() * scaleFactor
            } catch (e: NumberFormatException) {
                originalHeight.toDoubleOrNull() ?: 0.0
            }
            
            val scaledWidth = if (widthValue > 0) widthValue.toString() else originalWidth
            val scaledHeight = if (heightValue > 0) heightValue.toString() else originalHeight
            
            modified = modified.replace(Regex("""width\s*=\s*["'][^"']+["']"""), "width=\"$scaledWidth\"")
            modified = modified.replace(Regex("""height\s*=\s*["'][^"']+["']"""), "height=\"$scaledHeight\"")
        }
    }
    
    // Scale down SVG content for JVM by wrapping in a scaled group
    modified = applySvgScaling(modified)
    
    // Handle dentition-based tooth visibility (for JVM which doesn't support JavaScript)
    modified = applyDentitionVisibility(modified, state.patientInfo.dentition)
    
    // Update periodontal segments (sextants)
    for (segmentNum in 1..6) {
        val segment = state.gingivalSegments[segmentNum]
        if (segment != null) {
            modified = updateSextantInSvg(modified, segmentNum, segment)
        }
    }
    
    // Update tooth visualizations - update all teeth to handle status changes
    state.teeth.forEach { (toothNum, tooth) ->
        // Always update teeth to ensure visual properties are cleared when status changes
        modified = updateToothInSvg(modified, toothNum, tooth)
    }
    
    // Ensure all tooth number text elements are always black
    modified = ensureToothNumberTextIsBlack(modified, state.teeth.keys)
    
    return modified
}

private fun applySvgScaling(svg: String): String {
    // Check if scaling group already exists
    if (svg.contains("id=\"svg-scale-wrapper\"")) {
        return svg
    }
    
    // Find the opening <svg> tag and extract width/height
    val svgOpenMatch = Regex("""<svg([^>]*)>""").find(svg)
    if (svgOpenMatch == null) return svg
    
    val svgOpenEnd = svgOpenMatch.range.last + 1
    val svgAttrs = svgOpenMatch.groupValues[1]
    
    // Extract width and height for centering
    val widthMatch = Regex("""width\s*=\s*["']([^"']+)["']""").find(svgAttrs)
    val heightMatch = Regex("""height\s*=\s*["']([^"']+)["']""").find(svgAttrs)
    
    val width = widthMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 289.61
    val height = heightMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 370.54
    
    // Calculate center and scale transform
    // Scale to 0.9, then translate to center the scaled content
    val scale = 0.9
    val translateX = width * (1 - scale) / 2
    val translateY = height * (1 - scale) / 2
    
    // Find the closing </svg> tag
    val svgCloseMatch = Regex("""</svg>""").find(svg, svgOpenEnd)
    if (svgCloseMatch == null) return svg
    
    val svgCloseStart = svgCloseMatch.range.first
    
    // Extract the content between <svg> and </svg>
    val svgContent = svg.substring(svgOpenEnd, svgCloseStart)
    
    // Wrap content in a scaled and centered group
    val scaledContent = "<g id=\"svg-scale-wrapper\" transform=\"translate($translateX,$translateY) scale($scale)\">$svgContent</g>"
    
    // Reconstruct the SVG
    return svg.substring(0, svgOpenEnd) + scaledContent + svg.substring(svgCloseStart)
}

private fun applyDentitionVisibility(svg: String, dentition: String): String {
    val permanentTeeth = listOf(11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 38, 41, 42, 43, 44, 45, 46, 47, 48)
    val primaryTeeth = listOf(51, 52, 53, 54, 55, 61, 62, 63, 64, 65, 71, 72, 73, 74, 75, 81, 82, 83, 84, 85)
    
    var modified = svg
    
    when (dentition) {
        "Permanent", "permanent" -> {
            // Hide primary teeth, show permanent teeth
            primaryTeeth.forEach { toothNum ->
                modified = setToothDisplayStyle(modified, toothNum, "none")
            }
            permanentTeeth.forEach { toothNum ->
                modified = setToothDisplayStyle(modified, toothNum, "block")
            }
        }
        "Primary", "primary" -> {
            // Hide permanent teeth, show primary teeth
            permanentTeeth.forEach { toothNum ->
                modified = setToothDisplayStyle(modified, toothNum, "none")
            }
            primaryTeeth.forEach { toothNum ->
                modified = setToothDisplayStyle(modified, toothNum, "block")
            }
        }
        "Mixed", "mixed" -> {
            // Show all teeth
            (permanentTeeth + primaryTeeth).forEach { toothNum ->
                modified = setToothDisplayStyle(modified, toothNum, "block")
            }
        }
        "Edentulous", "edentulous" -> {
            // Hide all teeth
            (permanentTeeth + primaryTeeth).forEach { toothNum ->
                modified = setToothDisplayStyle(modified, toothNum, "none")
            }
        }
    }
    
    return modified
}

private fun setToothDisplayStyle(svg: String, toothNum: Int, display: String): String {
    // Find all elements with class containing "tooth-{toothNum}"
    val pattern = Regex("""(class\s*=\s*["'][^"']*tooth-$toothNum[^"']*["'])([^>]*style\s*=\s*["'])([^"']*)(["'])""", RegexOption.IGNORE_CASE)
    
    var modified = pattern.replace(svg) { matchResult ->
        val classAttr = matchResult.groupValues[1]
        val styleStart = matchResult.groupValues[2]
        val existingStyle = matchResult.groupValues[3]
        val styleEnd = matchResult.groupValues[4]
        
        // Add or update display in style
        val updatedStyle = if (existingStyle.contains("display:")) {
            existingStyle.replace(Regex("""display\s*:\s*[^;]+"""), "display:$display")
        } else {
            if (existingStyle.isNotEmpty() && !existingStyle.endsWith(";")) {
                "$existingStyle;display:$display"
            } else {
                "$existingStyle display:$display"
            }
        }
        
        "$classAttr$styleStart$updatedStyle$styleEnd"
    }
    
    // Handle elements without style attribute - add style attribute
    val patternNoStyle = Regex("""(class\s*=\s*["'][^"']*tooth-$toothNum[^"']*["'])([^>]*)(>)""", RegexOption.IGNORE_CASE)
    modified = patternNoStyle.replace(modified) { matchResult ->
        val classAttr = matchResult.groupValues[1]
        val otherAttrs = matchResult.groupValues[2]
        val closingBracket = matchResult.groupValues[3]
        
        // Only add if style doesn't already exist
        if (!otherAttrs.contains("style=")) {
            "$classAttr$otherAttrs style=\"display:$display\"$closingBracket"
        } else {
            matchResult.value
        }
    }
    
    return modified
}

private fun updateSextantInSvg(svg: String, segmentNum: Int, segment: GingivalSegment): String {
    // Check if segment has findings
    val hasFindings = segment.color != "Pink" ||
            segment.sizeEdema != "Normal" ||
            segment.consistency != "Firm and resilient" ||
            segment.texture != "Stippled (normal)" ||
            segment.calculus != "None"
    
    if (!hasFindings) {
        // Remove calculus overlay if present
        val overlayId = "calculus-overlay-$segmentNum"
        val overlayPattern = """<[^>]*id\s*=\s*["']$overlayId["'][^>]*>.*?</[^>]+>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        return svg.replace(overlayPattern, "")
    }
    
    // Base color - Color
    val fillColor = when (segment.color) {
        "Pink" -> "#FFB6C1"
        "Reddish-pink" -> "#FF6B9D"
        "Red" -> "#FF0000"
        "Dark red/cyanotic" -> "#8B0000"
        "Mixed" -> "#FF69B4"
        "Necrotic" -> "#2a2a2a"
        else -> "#FFB6C1"
    }
    
    // Stroke width - Size/Edema
    val strokeWidth = when (segment.sizeEdema) {
        "Recession" -> "2"
        "Normal" -> "4"
        "Mild" -> "8"
        "Moderate" -> "14"
        "Marked" -> "22"
        else -> "4"
    }
    
    // Opacity - Consistency
    val opacity = when (segment.consistency) {
        "Firm and resilient" -> "1.0"
        "Spongy" -> "0.85"
        "Soft/edematous" -> "0.7"
        "Hyperkeratotic" -> "0.95"
        "Friable" -> "0.6"
        "Retractile" -> "0.75"
        else -> "1.0"
    }
    
    // Stroke dash array - Texture
    val strokeDasharray = when (segment.texture) {
        "Stippled (normal)" -> ""
        "Loss of stippling" -> "5,5"
        "Smooth/shiny" -> "10,5"
        "Fibrotic/leathery" -> "2,2"
        "Ulcerated" -> "15,5,5,5"
        else -> ""
    }
    
    // Calculus overlay - apply pattern on top of base color when calculus is present
    val hasCalculus = segment.calculus != "None"
    val calculusOpacity = when (segment.calculus) {
        "Supragingival" -> "0.8"
        "Subgingival" -> "0.9"
        "Heavy" -> "1.0"
        else -> null
    }
    
    // Find element with id="sextant-{segmentNum}" and update its attributes
    val idPattern = """id\s*=\s*["']sextant-$segmentNum["']""".toRegex()
    val match = idPattern.find(svg)
    if (match == null) return svg
    
    val startIndex = match.range.first
    // Find the opening tag
    var tagStart = startIndex
    while (tagStart > 0 && svg[tagStart] != '<') tagStart--
    
    // Find the closing of the opening tag
    var tagEnd = startIndex
    while (tagEnd < svg.length && svg[tagEnd] != '>') tagEnd++
    if (tagEnd >= svg.length) return svg
    
    val beforeTag = svg.substring(0, tagStart)
    val tagContent = svg.substring(tagStart, tagEnd + 1)
    val afterTag = svg.substring(tagEnd + 1)
    
    // Check if tag is self-closing
    val isSelfClosing = tagContent.trimEnd().endsWith("/>")
    
    // Always use base color for the path (not calculus pattern)
    // Update attributes in tag
    var updatedTag = tagContent
        .replace(Regex("""fill\s*=\s*["'][^"']*["']"""), """fill="$fillColor"""")
        .replace(Regex("""stroke\s*=\s*["'][^"']*["']"""), """stroke="#333"""")
        .replace(Regex("""stroke-width\s*=\s*["'][^"']*["']"""), """stroke-width="$strokeWidth"""")
        .replace(Regex("""opacity\s*=\s*["'][^"']*["']"""), """opacity="$opacity"""")
    
    // Add missing attributes if they don't exist
    if (!updatedTag.contains("fill=")) {
        updatedTag = updatedTag.replace(if (isSelfClosing) "/>" else ">", """ fill="$fillColor"${if (isSelfClosing) "/>" else ">"}""")
    }
    if (!updatedTag.contains("stroke=")) {
        val closing = if (isSelfClosing) "/>" else ">"
        updatedTag = updatedTag.replace(closing, """ stroke="#333"$closing""")
    }
    if (!updatedTag.contains("stroke-width=")) {
        val closing = if (isSelfClosing) "/>" else ">"
        updatedTag = updatedTag.replace(closing, """ stroke-width="$strokeWidth"$closing""")
    }
    if (!updatedTag.contains("opacity=")) {
        val closing = if (isSelfClosing) "/>" else ">"
        updatedTag = updatedTag.replace(closing, """ opacity="$opacity"$closing""")
    }
    
    // Handle stroke-dasharray
    if (strokeDasharray.isNotEmpty()) {
        updatedTag = updatedTag.replace(Regex("""stroke-dasharray\s*=\s*["'][^"']*["']"""), """stroke-dasharray="$strokeDasharray"""")
        if (!updatedTag.contains("stroke-dasharray=")) {
            val closing = if (isSelfClosing) "/>" else ">"
            updatedTag = updatedTag.replace(closing, """ stroke-dasharray="$strokeDasharray"$closing""")
        }
    } else {
        updatedTag = updatedTag.replace(Regex("""\s*stroke-dasharray\s*=\s*["'][^"']*["']"""), "")
    }
    
    // Find the end of the path element (either self-closing or find closing tag)
    var pathEndPosition = tagEnd + 1
    if (!isSelfClosing) {
        // Find closing </path> tag
        val closingTagPattern = """</path>""".toRegex()
        val closingMatch = closingTagPattern.find(afterTag)
        if (closingMatch != null) {
            pathEndPosition = tagEnd + 1 + closingMatch.range.last + 1
        }
    }
    
    // Build result with updated tag
    var result = beforeTag + updatedTag
    
    // Add closing tag if not self-closing
    if (!isSelfClosing) {
        val closingTagPattern = """</path>""".toRegex()
        val closingMatch = closingTagPattern.find(afterTag)
        if (closingMatch != null) {
            result += closingMatch.value
        }
    }
    
    // Add or remove calculus overlay
    val overlayId = "calculus-overlay-$segmentNum"
    val existingOverlayPattern = """<use[^>]*id\s*=\s*["']$overlayId["'][^>]*/?>""".toRegex()
    result = result.replace(existingOverlayPattern, "")
    
    if (hasCalculus && calculusOpacity != null) {
        // Insert overlay right after the path element
        // Calculus appears as a yellow fill overlay on top of the gingival color
        val overlayElement = """<use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="#sextant-$segmentNum" id="$overlayId" fill="#FFD700" fill-opacity="$calculusOpacity" stroke="none" pointer-events="none"/>"""
        result = result.substring(0, pathEndPosition) + overlayElement + result.substring(pathEndPosition)
    }
    
    // Add remaining content after path
    val remainingAfterPath = if (isSelfClosing) {
        afterTag
    } else {
        val closingTagPattern = """</path>""".toRegex()
        val closingMatch = closingTagPattern.find(afterTag)
        if (closingMatch != null) {
            afterTag.substring(closingMatch.range.last + 1)
        } else {
            afterTag
        }
    }
    result += remainingAfterPath
    
    return result
}

private fun updateToothInSvg(svg: String, toothNum: Int, tooth: Tooth): String {
    val cariesProps = getToothCariesVisualProperties(tooth)
    // Always update teeth to clear previous visual properties when status changes
    // For normal teeth with no caries, use transparent/none to clear any previous styling
    val props = if (cariesProps == null && tooth.status == "normal" && !tooth.caries.contains("ICDAS") && !tooth.caries.startsWith("+")) {
        // Clear visual properties for normal teeth
        ToothVisualProperties(color = "none", opacity = 1.0)
    } else {
        cariesProps ?: ToothVisualProperties(color = "#FFFFFF", opacity = 1.0)
    }
    
    var result = svg
    // Find all elements with class containing "tooth-{toothNum}"
    val classPattern = """class\s*=\s*["'][^"']*tooth-$toothNum[^"']*["']""".toRegex()
    
    var matchCount = 0
    classPattern.findAll(result).forEach { match ->
        matchCount++
        val classStart = match.range.first
        val classEnd = match.range.last
        
        // Find the opening tag start (<) - search backwards from classStart
        var tagStart = classStart
        while (tagStart > 0 && tagStart > classStart - 200 && result[tagStart] != '<') {
            tagStart--
        }
        if (tagStart < 0 || result[tagStart] != '<') {
            return@forEach
        }
        
        // Find the tag name (path, circle, etc.)
        var tagNameEnd = tagStart + 1
        while (tagNameEnd < result.length && tagNameEnd < tagStart + 20 && result[tagNameEnd].isLetter()) {
            tagNameEnd++
        }
        if (tagNameEnd >= result.length) {
            return@forEach
        }
        
        val tagName = result.substring(tagStart + 1, tagNameEnd)
        
        // Find the closing > of the opening tag
        // Use a simpler approach: search forward from classEnd, tracking quotes
        var tagEnd = classEnd + 1
        var inQuotes = false
        var quoteChar: Char? = null
        
        // First, check if we're inside quotes at classEnd
        // Count quotes from tagStart to classEnd
        var quoteCount = 0
        var lastQuoteChar: Char? = null
        for (i in tagStart..classEnd) {
            if (result[i] == '"' || result[i] == '\'') {
                if (lastQuoteChar == null || result[i] == lastQuoteChar) {
                    quoteCount++
                    lastQuoteChar = result[i]
                }
            }
        }
        inQuotes = (quoteCount % 2 == 1)
        if (inQuotes && lastQuoteChar != null) {
            quoteChar = lastQuoteChar
            // Find the closing quote
            var quoteEnd = classEnd + 1
            while (quoteEnd < result.length && quoteEnd < classEnd + 500 && result[quoteEnd] != quoteChar) {
                quoteEnd++
            }
            if (quoteEnd < result.length) {
                tagEnd = quoteEnd + 1
                inQuotes = false
            }
        }
        
        // Now search for the closing > from tagEnd
        var foundClosing = false
        var searchLimit = minOf(tagEnd + 2000, result.length) // Limit search to 2000 chars from tagEnd
        while (tagEnd < searchLimit) {
            val char = result[tagEnd]
            when {
                !inQuotes && (char == '"' || char == '\'') -> {
                    inQuotes = true
                    quoteChar = char
                }
                inQuotes && char == quoteChar -> {
                    inQuotes = false
                    quoteChar = null
                }
                !inQuotes && char == '>' -> {
                    // Found the closing >
                    foundClosing = true
                    break
                }
            }
            tagEnd++
        }
        
        if (!foundClosing || tagEnd >= result.length || result[tagEnd] != '>') {
            return@forEach
        }
        
        val beforeTag = result.substring(0, tagStart)
        val tagContent = result.substring(tagStart, tagEnd + 1)
        val afterTag = result.substring(tagEnd + 1)
        
        // Skip text elements - tooth numbers should always be black
        if (tagName.equals("text", ignoreCase = true)) {
            // Ensure text elements have black fill/stroke
            val textFillPattern = Regex("""fill\s*=\s*["'][^"']*["']""")
            val textStrokePattern = Regex("""stroke\s*=\s*["'][^"']*["']""")
            val hasStyleAttr = tagContent.contains(Regex("""style\s*=\s*["']"""))
            
            if (hasStyleAttr) {
                val stylePattern = Regex("""style\s*=\s*["']([^"']*)["']""")
                val styleMatch = stylePattern.find(tagContent)
                if (styleMatch != null) {
                    var currentStyle = styleMatch.groupValues[1]
                    // Ensure fill is black for text
                    val fillPattern = Regex("""fill\s*:\s*[^;]+""")
                    if (fillPattern.containsMatchIn(currentStyle)) {
                        currentStyle = currentStyle.replace(fillPattern, "fill:#000000")
                    } else {
                        currentStyle = "fill:#000000;$currentStyle"
                    }
                    // Ensure stroke is black for text (if stroke exists)
                    val strokePattern = Regex("""stroke\s*:\s*[^;]+""")
                    if (strokePattern.containsMatchIn(currentStyle)) {
                        currentStyle = currentStyle.replace(strokePattern, "stroke:#000000")
                    }
                    val updatedTag = tagContent.replace(stylePattern, """style="$currentStyle"""")
                    result = beforeTag + updatedTag + afterTag
                }
            } else {
                // Use separate attributes
                var updatedTag = tagContent
                updatedTag = updatedTag.replace(textFillPattern, """fill="#000000"""")
                if (updatedTag.contains("stroke=")) {
                    updatedTag = updatedTag.replace(textStrokePattern, """stroke="#000000"""")
                }
                result = beforeTag + updatedTag + afterTag
            }
            return@forEach
        }
        
        // Update or add attributes
        var updatedTag = tagContent
        
        // Check if element uses style attribute or separate attributes
        val hasStyleAttr = updatedTag.contains(Regex("""style\s*=\s*["']"""))
        
        if (hasStyleAttr) {
            // Parse and modify style attribute
            val stylePattern = Regex("""style\s*=\s*["']([^"']*)["']""")
            val styleMatch = stylePattern.find(updatedTag)
            
            if (styleMatch != null) {
                val currentStyle = styleMatch.groupValues[1]
                // Parse style string and update fill and opacity
                var newStyle = currentStyle
                
                // Update fill in style - replace any existing fill value
                // For normal teeth (fill="none"), remove fill and opacity to restore default appearance
                if (props.color == "none") {
                    // Remove fill and opacity for normal teeth to clear previous styling
                    val fillPattern = Regex("""fill\s*:\s*[^;]+;?\s*""")
                    val opacityPattern = Regex("""opacity\s*:\s*[^;]+;?\s*""")
                    newStyle = newStyle.replace(fillPattern, "")
                    newStyle = newStyle.replace(opacityPattern, "")
                    newStyle = newStyle.trim(';', ' ')
                } else {
                    val fillPattern = Regex("""fill\s*:\s*[^;]+""")
                    if (fillPattern.containsMatchIn(newStyle)) {
                        newStyle = newStyle.replace(fillPattern, """fill:${props.color}""")
                    } else {
                        // Add fill at the beginning if it doesn't exist
                        newStyle = "fill:${props.color};$newStyle"
                    }
                    
                    // Update opacity in style (or add it) - but only if opacity < 1.0
                    // For full opacity, we can omit it or set it explicitly
                    val opacityPattern = Regex("""opacity\s*:\s*[^;]+""")
                    if (opacityPattern.containsMatchIn(newStyle)) {
                        newStyle = newStyle.replace(opacityPattern, """opacity:${props.opacity}""")
                    } else {
                        // Add opacity after fill if opacity < 1.0
                        if (props.opacity < 1.0) {
                            newStyle = newStyle.replaceFirst("fill:${props.color}", """fill:${props.color};opacity:${props.opacity}""")
                        }
                    }
                }
                
                // Update stroke-width if needed
                if (props.strokeWidth > 0) {
                    val strokeWidthPattern = Regex("""stroke-width\s*:\s*[^;]+""")
                    if (strokeWidthPattern.containsMatchIn(newStyle)) {
                        newStyle = newStyle.replace(strokeWidthPattern, """stroke-width:${props.strokeWidth}""")
                    } else {
                        newStyle = "$newStyle;stroke-width:${props.strokeWidth}"
                    }
                }
                
                // Update stroke color if needed
                if (props.strokeColor.isNotEmpty()) {
                    val strokePattern = Regex("""stroke\s*:\s*[^;]+""")
                    if (strokePattern.containsMatchIn(newStyle)) {
                        newStyle = newStyle.replace(strokePattern, """stroke:${props.strokeColor}""")
                    } else {
                        newStyle = "$newStyle;stroke:${props.strokeColor}"
                    }
                }
                
                // Replace the style attribute
                updatedTag = updatedTag.replace(stylePattern, """style="$newStyle"""")
            }
        } else {
            // Use separate attributes (fallback)
            updatedTag = updatedTag.replace(Regex("""fill\s*=\s*["'][^"']*["']"""), """fill="${props.color}"""")
            updatedTag = updatedTag.replace(Regex("""opacity\s*=\s*["'][^"']*["']"""), """opacity="${props.opacity}"""")
            
            // Build attributes to add if missing
            val attributesToAdd = StringBuilder()
            val lastGtIndex = updatedTag.lastIndexOf('>')
            if (lastGtIndex >= 0) {
                val tagContentBeforeGt = updatedTag.substring(0, lastGtIndex)
                if (!tagContentBeforeGt.contains("fill=")) {
                    attributesToAdd.append(""" fill="${props.color}"""")
                }
                if (!tagContentBeforeGt.contains("opacity=")) {
                    attributesToAdd.append(""" opacity="${props.opacity}"""")
                }
                
                if (props.strokeWidth > 0) {
                    updatedTag = updatedTag.replace(Regex("""stroke-width\s*=\s*["'][^"']*["']"""), """stroke-width="${props.strokeWidth}"""")
                    // Re-check after replacement
                    val newLastGtIndex = updatedTag.lastIndexOf('>')
                    if (newLastGtIndex >= 0 && !updatedTag.substring(0, newLastGtIndex).contains("stroke-width=")) {
                        attributesToAdd.append(""" stroke-width="${props.strokeWidth}"""")
                    }
                }
                
                if (props.strokeColor.isNotEmpty()) {
                    updatedTag = updatedTag.replace(Regex("""stroke\s*=\s*["'][^"']*["']"""), """stroke="${props.strokeColor}"""")
                    // Re-check after replacement
                    val newLastGtIndex = updatedTag.lastIndexOf('>')
                    if (newLastGtIndex >= 0 && !updatedTag.substring(0, newLastGtIndex).contains("stroke=")) {
                        attributesToAdd.append(""" stroke="${props.strokeColor}"""")
                    }
                }
                
                // Insert new attributes before the closing >
                if (attributesToAdd.isNotEmpty()) {
                    val finalGtIndex = updatedTag.lastIndexOf('>')
                    if (finalGtIndex >= 0 && finalGtIndex < updatedTag.length) {
                        updatedTag = updatedTag.substring(0, finalGtIndex) + attributesToAdd.toString() + updatedTag.substring(finalGtIndex)
                    }
                }
            }
        }
        
        // Safety check: ensure the tag still has a closing > and starts with <
        if (!updatedTag.startsWith("<") || !updatedTag.contains('>')) {
            // If we somehow corrupted the tag, skip this modification
            return@forEach
        }
        
        result = beforeTag + updatedTag + afterTag
    }
    
    return result
}

private fun ensureToothNumberTextIsBlack(svg: String, toothNumbers: Set<Int>): String {
    var result = svg
    
    // Find all text elements with tooth number classes and ensure they have black fill/stroke
    toothNumbers.forEach { toothNum ->
        // Pattern to find text elements with class containing "tooth-{toothNum}"
        // Match text tags that have the tooth class anywhere in their attributes
        val textPattern = Regex("""<text([^>]*class\s*=\s*["'][^"']*tooth-$toothNum[^"']*["'][^>]*)>""", RegexOption.IGNORE_CASE)
        
        result = textPattern.replace(result) { matchResult ->
            val textAttrs = matchResult.groupValues[1]
            var updatedAttrs = textAttrs
            
            // Check if it has style attribute
            val hasStyleAttr = textAttrs.contains(Regex("""style\s*=\s*["']"""))
            
            if (hasStyleAttr) {
                val stylePattern = Regex("""style\s*=\s*["']([^"']*)["']""")
                val styleMatch = stylePattern.find(textAttrs)
                if (styleMatch != null) {
                    var currentStyle = styleMatch.groupValues[1]
                    // Ensure fill is black
                    val fillPattern = Regex("""fill\s*:\s*[^;]+""")
                    if (fillPattern.containsMatchIn(currentStyle)) {
                        currentStyle = currentStyle.replace(fillPattern, "fill:#000000")
                    } else {
                        currentStyle = "fill:#000000;$currentStyle"
                    }
                    // Ensure stroke is black (if stroke exists)
                    val strokePattern = Regex("""stroke\s*:\s*[^;]+""")
                    if (strokePattern.containsMatchIn(currentStyle)) {
                        currentStyle = currentStyle.replace(strokePattern, "stroke:#000000")
                    }
                    updatedAttrs = textAttrs.replace(stylePattern, """style="$currentStyle"""")
                }
            } else {
                // Use separate attributes
                val fillPattern = Regex("""fill\s*=\s*["'][^"']*["']""")
                val strokePattern = Regex("""stroke\s*=\s*["'][^"']*["']""")
                
                updatedAttrs = updatedAttrs.replace(fillPattern, """fill="#000000"""")
                if (updatedAttrs.contains("stroke=")) {
                    updatedAttrs = updatedAttrs.replace(strokePattern, """stroke="#000000"""")
                } else {
                    // Add black stroke if no stroke exists (for better visibility)
                    val lastGtIndex = updatedAttrs.lastIndexOf('>')
                    if (lastGtIndex < 0) {
                        updatedAttrs += """ stroke="#000000""""
                    } else {
                        updatedAttrs = updatedAttrs.substring(0, lastGtIndex) + """ stroke="#000000"""" + updatedAttrs.substring(lastGtIndex)
                    }
                }
            }
            
            "<text$updatedAttrs>"
        }
    }
    
    return result
}

// Data class for tooth visual properties
internal data class ToothVisualProperties(
    val color: String,
    val opacity: Double = 1.0,
    val strokeWidth: Double = 0.0,
    val strokeColor: String = ""
)

// Get visual properties for a tooth based on its status and conditions
internal fun getToothCariesVisualProperties(tooth: Tooth): ToothVisualProperties? {
    // Handle missing teeth - black fill
    if (tooth.status == "missing") {
        return ToothVisualProperties(
            color = "#000000", // Black
            opacity = 1.0
        )
    }
    
    // Handle treated teeth - green fill
    if (tooth.status == "treated") {
        return ToothVisualProperties(
            color = "#00FF00", // Green
            opacity = 1.0
        )
    }
    
    // Handle not examined teeth - gray fill
    if (tooth.status == "not examined") {
        return ToothVisualProperties(
            color = "#808080", // Gray
            opacity = 0.5
        )
    }
    
    // Check for pain parameters first - pink fill
    val hasPain = tooth.painStatus != "0 = No pain (asymptomatic)" ||
                  tooth.tenderOnPercussion != "0 = Negative" ||
                  tooth.vestibularTenderness != "0 = Negative" ||
                  tooth.sinusTract != "0 = Normal (no sinus tract)"
    
    if (hasPain) {
        return ToothVisualProperties(
            color = "#FFC0CB", // Pink
            opacity = 0.8
        )
    }
    
    // Handle caries visualization (ICDAS) - brown fill
    val cariesValue = tooth.caries
    if (cariesValue.startsWith("+") || cariesValue.contains("ICDAS")) {
        val cariesLevel = when {
            cariesValue.contains("ICDAS 0") -> 0
            cariesValue.contains("ICDAS 1") -> 1
            cariesValue.contains("ICDAS 2") -> 2
            cariesValue.contains("ICDAS 3") -> 3
            cariesValue.contains("ICDAS 4") -> 4
            cariesValue.contains("ICDAS 5") -> 5
            cariesValue.contains("ICDAS 6") -> 6
            cariesValue.contains("Root caries - Active") -> 7
            cariesValue.contains("Root caries - Cavitated") -> 8
            else -> 0
        }
        
        if (cariesLevel > 0) {
            // Color based on caries severity (brown/orange shades)
            val color = when (cariesLevel) {
                1, 2 -> "#FFA500" // Light orange
                3, 4 -> "#FF8C00" // Dark orange
                5, 6 -> "#8B4513" // Brown
                7, 8 -> "#654321" // Dark brown
                else -> "#FFA500"
            }
            
            val opacity = when (cariesLevel) {
                1, 2 -> 0.3
                3, 4 -> 0.5
                5, 6 -> 0.7
                7, 8 -> 0.9
                else -> 0.3
            }
            
            return ToothVisualProperties(
                color = color,
                opacity = opacity
            )
        }
    }
    
    // Check for other abnormal conditions - red fill
    // Exclude "not examined" from abnormal conditions
    val hasOtherAbnormalities = (tooth.status == "abnormal") ||
                                (tooth.erosion != "0 = No erosive wear" && tooth.erosion.isNotEmpty()) ||
                                (tooth.fractureType != "0 = No fracture" && tooth.fractureType.isNotEmpty()) ||
                                (tooth.attrition != "0 = No attrition" && tooth.attrition.isNotEmpty()) ||
                                (tooth.abrasion != "0 = No abrasion" && tooth.abrasion.isNotEmpty()) ||
                                (tooth.abfraction != "0 = No abfraction" && tooth.abfraction.isNotEmpty()) ||
                                (tooth.discoloration != "0 = Normal tooth color" && tooth.discoloration.isNotEmpty() && !tooth.discoloration.contains("Pink")) ||
                                (tooth.toothSize != "0 = Normal (±1.5mm)" && tooth.toothSize.isNotEmpty()) ||
                                (tooth.toothShape != "0 = Normal shape" && tooth.toothShape.isNotEmpty()) ||
                                (tooth.toothMorphology != "0 = Normal morphology" && tooth.toothMorphology.isNotEmpty()) ||
                                (tooth.eruptionStatus != "0 = Normal eruption" && tooth.eruptionStatus.isNotEmpty())
    
    if (hasOtherAbnormalities) {
        return ToothVisualProperties(
            color = "#FF0000", // Red
            opacity = 0.7
        )
    }
    
    // Return null if no special visualization is needed (tooth appears normal)
    return null
}

