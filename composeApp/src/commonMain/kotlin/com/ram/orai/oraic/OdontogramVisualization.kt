package com.ram.orai.oraic

/**
 * Generates JavaScript code to update SVG visualization based on DentalState
 */
fun generateVisualizationScript(state: DentalState): String {
    val script = StringBuilder()
    
    script.append("""
        (function() {
            try {
                // Initialize result array to collect all messages
                var result = [];
                var messages = [];
                
                // Ensure console messages array exists
                if (!window.consoleMessages) {
                    window.consoleMessages = [];
                }
                
                function logMsg(msg) {
                    var message = String(msg);
                    messages.push(message);
                    window.consoleMessages.push(message);
                    if (window.consoleMessages.length > 100) {
                        window.consoleMessages.shift();
                    }
                    if (window.AndroidLog && window.AndroidLog.log) {
                        try {
                            window.AndroidLog.log(message);
                        } catch(e) {
                            result.push('AndroidLog error: ' + e);
                        }
                    }
                    try {
                        console.log(message);
                    } catch(e) {
                        result.push('console.log error: ' + e);
                    }
                }
                
                result.push('=== Script started ===');
                logMsg('Starting visualization script...');
                
                const svg = document.querySelector('svg');
                if (!svg) {
                    var error = 'SVG not found in DOM';
                    logMsg(error);
                    result.push(error);
                    result.push('Messages: ' + messages.join(' | '));
                    window.lastScriptResult = result.join('\\n');
                    window.lastScriptMessages = messages;
                    return result.join('\\n');
                }
                logMsg('SVG found, proceeding with visualization');
                result.push('SVG found: ' + svg.getAttribute('width') + 'x' + svg.getAttribute('height'));
            logMsg('SVG dimensions: ' + svg.getAttribute('width') + ' x ' + svg.getAttribute('height'));
            logMsg('SVG viewBox: ' + svg.getAttribute('viewBox'));
            
            // Ensure SVG is visible
            svg.style.display = 'block';
            svg.style.visibility = 'visible';
            svg.style.opacity = '1';
            svg.style.width = 'auto';
            svg.style.height = 'auto';
            svg.style.maxWidth = '90%';
            svg.style.maxHeight = '85vh';
                logMsg('SVG visibility set');
                
                // Force body and html to be visible
                document.body.style.display = 'block';
                document.body.style.visibility = 'visible';
                document.body.style.opacity = '1';
                document.documentElement.style.display = 'block';
                document.documentElement.style.visibility = 'visible';
                logMsg('Body and HTML visibility forced');
                
                // Check SVG computed styles
                const svgStyle = window.getComputedStyle(svg);
                logMsg('SVG computed: display=' + svgStyle.display + ', visibility=' + svgStyle.visibility + ', opacity=' + svgStyle.opacity);
                logMsg('SVG computed: width=' + svgStyle.width + ', height=' + svgStyle.height);
                logMsg('SVG clientWidth=' + svg.clientWidth + ', clientHeight=' + svg.clientHeight);
                logMsg('SVG offsetWidth=' + svg.offsetWidth + ', offsetHeight=' + svg.offsetHeight);
                
                // Make ALL SVG content visible by default (except teeth which we'll handle separately)
                // This ensures the base structure, labels, etc. are visible
                const allElements = svg.querySelectorAll('*');
                logMsg('Total SVG elements: ' + allElements.length);
                
                // Make sure at least some base elements are visible
                let visibleCount = 0;
                allElements.forEach(function(el) {
                    // Only modify elements that don't have tooth classes
                    var className = el.className;
                    var classStr = '';
                    if (className) {
                        if (typeof className === 'string') {
                            classStr = className;
                        } else if (className.baseVal) {
                            classStr = className.baseVal;
                        }
                    }
                    if (classStr.indexOf('tooth-') === -1) {
                        el.style.display = '';
                        el.style.visibility = '';
                        el.removeAttribute('display');
                        el.removeAttribute('visibility');
                        const elStyle = window.getComputedStyle(el);
                        if (elStyle.display !== 'none' && elStyle.visibility !== 'hidden') {
                            visibleCount++;
                        }
                    }
                });
                logMsg('Made all non-tooth elements visible. Count of visible non-tooth elements: ' + visibleCount);
                
                // Make sure all paths and groups are visible initially (before tooth-specific hiding)
                const allPaths = svg.querySelectorAll('path, g, circle, rect, ellipse');
                logMsg('Total SVG elements (path/g/circle/rect/ellipse): ' + allPaths.length);
                
                // Check if there are any visible elements before we hide teeth
                let visibleBefore = 0;
                allPaths.forEach(function(el) {
                    const style = window.getComputedStyle(el);
                    if (style.display !== 'none' && style.visibility !== 'hidden') {
                        visibleBefore++;
                    }
                });
                logMsg('Visible elements before tooth processing: ' + visibleBefore);
                
                // Handle dentition-based tooth visibility
                const dentition = '${state.patientInfo.dentition}';
                logMsg('Dentition: ' + dentition);
                result.push('Processing dentition: ' + dentition);
            
            // Define tooth number ranges
            const permanentTeeth = [11,12,13,14,15,16,17,18,21,22,23,24,25,26,27,28,31,32,33,34,35,36,37,38,41,42,43,44,45,46,47,48];
            const primaryTeeth = [51,52,53,54,55,61,62,63,64,65,71,72,73,74,75,81,82,83,84,85];
            
            // Function to show/hide teeth by number
            function setToothVisibility(toothNum, visible) {
                const selector = '[class*="tooth-' + toothNum + '"]';
                const elements = document.querySelectorAll(selector);
                logMsg('Setting visibility for tooth ' + toothNum + ': ' + visible + ' (found ' + elements.length + ' elements)');
                let changed = 0;
                elements.forEach(function(element) {
                    if (visible) {
                        element.style.display = '';
                        element.style.visibility = '';
                        element.removeAttribute('display');
                        element.removeAttribute('visibility');
                        changed++;
                    } else {
                        element.style.display = 'none';
                        element.style.visibility = 'hidden';
                    }
                });
                if (visible && changed > 0) {
                    logMsg('Made ' + changed + ' elements visible for tooth ' + toothNum);
                }
            }
            
            // Count total tooth elements before hiding
            const allToothElements = document.querySelectorAll('[class*="tooth-"]');
            logMsg('Total tooth elements found: ' + allToothElements.length);
            
            // DON'T hide all teeth initially - only hide specific ones based on dentition
            // This way the SVG structure remains visible
            logMsg('Skipping initial hide - will hide selectively based on dentition');
            
            // Show/hide teeth based on dentition selection
            if (dentition === 'permanent' || dentition === 'Permanent') {
                logMsg('Showing permanent teeth, hiding primary');
                // Hide primary teeth explicitly
                primaryTeeth.forEach(function(toothNum) {
                    setToothVisibility(toothNum, false);
                });
                // Show permanent teeth
                permanentTeeth.forEach(function(toothNum) {
                    setToothVisibility(toothNum, true);
                });
                logMsg('Permanent teeth visibility set');
            } else if (dentition === 'mixed' || dentition === 'Mixed') {
                logMsg('Showing all teeth (mixed)');
                // Show all teeth (both primary and permanent)
                document.querySelectorAll('[class*="tooth-"]').forEach(function(element) {
                    element.style.display = '';
                    element.style.visibility = '';
                    element.removeAttribute('display');
                    element.removeAttribute('visibility');
                });
                logMsg('All teeth shown');
            } else if (dentition === 'primary' || dentition === 'Primary') {
                logMsg('Showing primary teeth, hiding permanent');
                // Hide permanent teeth explicitly
                permanentTeeth.forEach(function(toothNum) {
                    setToothVisibility(toothNum, false);
                });
                // Show primary teeth
                primaryTeeth.forEach(function(toothNum) {
                    setToothVisibility(toothNum, true);
                });
                logMsg('Primary teeth visibility set');
            } else {
                logMsg('Edentulous - hiding all teeth');
                // Hide all teeth for edentulous
                allToothElements.forEach(function(element) {
                    element.style.display = 'none';
                    element.style.visibility = 'hidden';
                });
            }
            // Edentulous: all teeth remain hidden (already set above)
            
            // Final check - count visible teeth
            const visibleTeeth = document.querySelectorAll('[class*="tooth-"]');
            let visibleCount = 0;
            visibleTeeth.forEach(function(el) {
                const style = window.getComputedStyle(el);
                if (style.display !== 'none' && style.visibility !== 'hidden') {
                    visibleCount++;
                }
            });
            logMsg('Visible teeth after processing: ' + visibleCount + ' out of ' + visibleTeeth.length);
            
            // Check overall SVG visibility
            const computedStyle = window.getComputedStyle(svg);
            logMsg('SVG computed display: ' + computedStyle.display);
            logMsg('SVG computed visibility: ' + computedStyle.visibility);
            logMsg('SVG computed opacity: ' + computedStyle.opacity);
            logMsg('SVG computed width: ' + computedStyle.width);
            logMsg('SVG computed height: ' + computedStyle.height);
            
            // Try to force visibility of SVG container
            const svgParent = svg.parentElement;
            if (svgParent) {
                svgParent.style.display = 'block';
                svgParent.style.visibility = 'visible';
                logMsg('SVG parent visibility set');
            }
            
            logMsg('Starting periodontal segments processing');
            
            // Update periodontal segments (sextants)
    """.trimIndent())
    
    // Generate sextant visualization code
    for (segmentNum in 1..6) {
        val segment = state.gingivalSegments[segmentNum]
        if (segment != null) {
            script.append(generateSextantVisualization(segmentNum, segment))
        }
    }
    
    script.append("""
            
            // Update tooth visualizations
    """.trimIndent())
    
    // Generate tooth visualization code
    state.teeth.forEach { (toothNum, tooth) ->
        script.append(generateToothVisualization(toothNum, tooth))
    }
    
    script.append("""
            
                // Return console messages for debugging
                result.push('=== Script completed ===');
                result.push('Total messages logged: ' + messages.length);
                result.push('Window.consoleMessages length: ' + (window.consoleMessages ? window.consoleMessages.length : 'null'));
                result.push('=== All Messages ===');
                result.push(messages.join('\\n'));
                result.push('=== End Messages ===');
                
                // Also store in window for retrieval
                window.lastScriptResult = result.join('\\n');
                window.lastScriptMessages = messages;
                
                return result.join('\\n');
            } catch(error) {
                var errorMsg = 'Script error: ' + error.toString() + ' at ' + (error.stack || 'unknown');
                if (window.AndroidLog && window.AndroidLog.log) {
                    window.AndroidLog.log('ERROR: ' + errorMsg);
                }
                console.error(errorMsg);
                return 'ERROR: ' + errorMsg;
            }
        })();
    """.trimIndent())
    
    return script.toString()
}

private fun generateSextantVisualization(segmentNum: Int, segment: GingivalSegment): String {
    val script = StringBuilder()
    
    // Base color - Color
    val fillColor = when (segment.color) {
        "Pink" -> "#FFB6C1"
        "Reddish-pink" -> "#FF6B9D"
        "Red" -> "#FF0000"
        "Dark red/cyanotic" -> "#8B0000"
        "Mixed" -> "#FF69B4"
        "Necrotic" -> "#2a2a2a"
        else -> "#FFB6C1" // Default pink
    }
    
    // Stroke width - Size/Edema
    val strokeWidth = when (segment.sizeEdema) {
        "Recession" -> "2"
        "Normal" -> "4"
        "Mild" -> "8"
        "Moderate" -> "10"
        "Marked" -> "12"
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
    
    // Check if segment has findings (not all normal, excluding calculus for base path)
    val hasFindings = segment.color != "Pink" ||
            segment.sizeEdema != "Normal" ||
            segment.consistency != "Firm and resilient" ||
            segment.texture != "Stippled (normal)" ||
            segment.calculus != "None"
    
    if (hasFindings) {
        script.append("""
            {
                const path = svg.querySelector('#sextant-$segmentNum');
                if (path) {
                    // Always set base gingival color
                    path.setAttribute('fill', '$fillColor');
                    path.setAttribute('stroke', '#333');
                    path.setAttribute('stroke-width', '$strokeWidth');
                    path.setAttribute('opacity', '$opacity');
                    ${if (strokeDasharray.isNotEmpty()) "path.setAttribute('stroke-dasharray', '$strokeDasharray');" else "path.removeAttribute('stroke-dasharray');"}
                    
                    // Add calculus as stroke/border outside if present
                    ${if (hasCalculus && calculusOpacity != null) """
                    // Remove existing overlay if present
                    const existingOverlay = svg.querySelector('#calculus-overlay-$segmentNum');
                    if (existingOverlay) existingOverlay.remove();
                    
                    // Create overlay using <use> element to duplicate the path
                    // This will show calculus as a stroke/border outside the gingival fill
                    const overlay = document.createElementNS('http://www.w3.org/2000/svg', 'use');
                    overlay.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', '#sextant-$segmentNum');
                    overlay.setAttribute('id', 'calculus-overlay-$segmentNum');
                    overlay.setAttribute('fill', '#FFD700');
                    overlay.setAttribute('fill-opacity', '$calculusOpacity');
                    overlay.setAttribute('stroke', 'none');
                    overlay.setAttribute('pointer-events', 'none');
                    
                    // Insert overlay after the path element so it appears on top
                    if (path.nextSibling) {
                        path.parentNode.insertBefore(overlay, path.nextSibling);
                    } else {
                        path.parentNode.appendChild(overlay);
                    }
                    """ else """
                    // Remove calculus overlay if no calculus
                    const existingOverlay = svg.querySelector('#calculus-overlay-$segmentNum');
                    if (existingOverlay) existingOverlay.remove();
                    """}
                }
            }
        """.trimIndent())
    }
    
    return script.toString()
}

private fun generateToothVisualization(toothNum: Int, tooth: Tooth): String {
    val script = StringBuilder()
    
    // Get caries visual properties
    val cariesProps = getToothCariesVisualProperties(tooth)
    
    if (cariesProps != null) {
        // SVG uses class selectors like "tooth-11", "tooth-12", etc.
        // We need to find all elements with this class and update them
        script.append("""
            {
                const toothElements = svg.querySelectorAll('.tooth-$toothNum');
                if (toothElements && toothElements.length > 0) {
                    toothElements.forEach(function(element) {
                        element.setAttribute('fill', '${cariesProps.color}');
                        element.setAttribute('opacity', '${cariesProps.opacity}');
                        ${if (cariesProps.strokeWidth > 0.0) "element.setAttribute('stroke-width', '${cariesProps.strokeWidth}');" else ""}
                        ${if (cariesProps.strokeColor.isNotEmpty()) "element.setAttribute('stroke', '${cariesProps.strokeColor}');" else ""}
                    });
                }
            }
        """.trimIndent())
    }
    
    return script.toString()
}

// Note: ToothVisualProperties and getToothCariesVisualProperties are now defined in SvgXmlModifier.kt
// This file uses the same function from there

