function Image(img)
  local src = img.src or ""

  if src:find("screenshots") then
    -- Phone screenshots are portrait (720x1560). Cap height at 9cm so the
    -- surrounding description text fits with the image on a single page.
    img.attributes.height = "9cm"
    img.attributes.width  = nil
  elseif src:find("diagrams") then
    -- Diagrams get full content width so they are readable.
    img.attributes.width  = "16cm"
    img.attributes.height = nil
  else
    -- Fallback for any other images (logo, etc.)
    img.attributes.width  = "50%"
    img.attributes.height = nil
  end

  return img
end

function Header(el)
  -- Page break before every H3 (each screen section starts on a new page)
  if el.level == 3 then
    local pagebreak = pandoc.RawBlock('openxml',
      '<w:p><w:r><w:br w:type="page"/></w:r></w:p>')
    return {pagebreak, el}
  end
  -- Page break before H2 sections as well
  if el.level == 2 then
    local pagebreak = pandoc.RawBlock('openxml',
      '<w:p><w:r><w:br w:type="page"/></w:r></w:p>')
    return {pagebreak, el}
  end
  return el
end
