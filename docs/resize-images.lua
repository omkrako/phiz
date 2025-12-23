function Image(img)
  -- Set image width to 50% (you can adjust this percentage)
  img.attributes.width = "50%"
  return img
end

function Header(el)
  -- Insert page break before heading level 2
  if el.level == 2 then
    local pagebreak = pandoc.RawBlock('openxml', '<w:p><w:r><w:br w:type="page"/></w:r></w:p>')
    return {pagebreak, el}
  end
  return el
end
