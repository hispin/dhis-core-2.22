OpenLayers.Renderer.SVG=OpenLayers.Class(OpenLayers.Renderer.Elements,{xmlns:"http://www.w3.org/2000/svg",xlinkns:"http://www.w3.org/1999/xlink",MAX_PIXEL:15000,translationParameters:null,symbolSize:{},isGecko:null,initialize:function(containerID){if(!this.supported()){return;}
OpenLayers.Renderer.Elements.prototype.initialize.apply(this,arguments);this.translationParameters={x:0,y:0};this.isGecko=(navigator.userAgent.toLowerCase().indexOf("gecko/")!=-1);},destroy:function(){OpenLayers.Renderer.Elements.prototype.destroy.apply(this,arguments);},supported:function(){var svgFeature="http://www.w3.org/TR/SVG11/feature#";return(document.implementation&&(document.implementation.hasFeature("org.w3c.svg","1.0")||document.implementation.hasFeature(svgFeature+"SVG","1.1")||document.implementation.hasFeature(svgFeature+"BasicStructure","1.1")));},inValidRange:function(x,y,xyOnly){var left=x+(xyOnly?0:this.translationParameters.x);var top=y+(xyOnly?0:this.translationParameters.y);return(left>=-this.MAX_PIXEL&&left<=this.MAX_PIXEL&&top>=-this.MAX_PIXEL&&top<=this.MAX_PIXEL);},setExtent:function(extent,resolutionChanged){OpenLayers.Renderer.Elements.prototype.setExtent.apply(this,arguments);var resolution=this.getResolution();var left=-extent.left/resolution;var top=extent.top/resolution;if(resolutionChanged){this.left=left;this.top=top;var extentString="0 0 "+this.size.w+" "+this.size.h;this.rendererRoot.setAttributeNS(null,"viewBox",extentString);this.translate(0,0);return true;}else{var inRange=this.translate(left-this.left,top-this.top);if(!inRange){this.setExtent(extent,true);}
return inRange;}},translate:function(x,y){if(!this.inValidRange(x,y,true)){return false;}else{var transformString="";if(x||y){transformString="translate("+x+","+y+")";}
this.root.setAttributeNS(null,"transform",transformString);this.translationParameters={x:x,y:y};return true;}},setSize:function(size){OpenLayers.Renderer.prototype.setSize.apply(this,arguments);this.rendererRoot.setAttributeNS(null,"width",this.size.w);this.rendererRoot.setAttributeNS(null,"height",this.size.h);},getNodeType:function(geometry,style){var nodeType=null;switch(geometry.CLASS_NAME){case"OpenLayers.Geometry.Point":if(style.externalGraphic){nodeType="image";}else if(this.isComplexSymbol(style.graphicName)){nodeType="use";}else{nodeType="circle";}
break;case"OpenLayers.Geometry.Rectangle":nodeType="rect";break;case"OpenLayers.Geometry.LineString":nodeType="polyline";break;case"OpenLayers.Geometry.LinearRing":nodeType="polygon";break;case"OpenLayers.Geometry.Polygon":case"OpenLayers.Geometry.Curve":case"OpenLayers.Geometry.Surface":nodeType="path";break;default:break;}
return nodeType;},setStyle:function(node,style,options){style=style||node._style;options=options||node._options;var r=parseFloat(node.getAttributeNS(null,"r"));var widthFactor=1;var pos;if(node._geometryClass=="OpenLayers.Geometry.Point"&&r){node.style.visibility="";if(style.graphic===false){node.style.visibility="hidden";}else if(style.externalGraphic){pos=this.getPosition(node);if(style.graphicTitle){node.setAttributeNS(null,"title",style.graphicTitle);}
if(style.graphicWidth&&style.graphicHeight){node.setAttributeNS(null,"preserveAspectRatio","none");}
var width=style.graphicWidth||style.graphicHeight;var height=style.graphicHeight||style.graphicWidth;width=width?width:style.pointRadius*2;height=height?height:style.pointRadius*2;var xOffset=(style.graphicXOffset!=undefined)?style.graphicXOffset:-(0.5*width);var yOffset=(style.graphicYOffset!=undefined)?style.graphicYOffset:-(0.5*height);var opacity=style.graphicOpacity||style.fillOpacity;node.setAttributeNS(null,"x",(pos.x+xOffset).toFixed());node.setAttributeNS(null,"y",(pos.y+yOffset).toFixed());node.setAttributeNS(null,"width",width);node.setAttributeNS(null,"height",height);node.setAttributeNS(this.xlinkns,"href",style.externalGraphic);node.setAttributeNS(null,"style","opacity: "+opacity);}else if(this.isComplexSymbol(style.graphicName)){var offset=style.pointRadius*3;var size=offset*2;var id=this.importSymbol(style.graphicName);var href="#"+id;pos=this.getPosition(node);widthFactor=this.symbolSize[id]/size;var parent=node.parentNode;var nextSibling=node.nextSibling;if(parent){parent.removeChild(node);}
node.setAttributeNS(this.xlinkns,"href",href);node.setAttributeNS(null,"width",size);node.setAttributeNS(null,"height",size);node.setAttributeNS(null,"x",pos.x-offset);node.setAttributeNS(null,"y",pos.y-offset);if(nextSibling){parent.insertBefore(node,nextSibling);}else if(parent){parent.appendChild(node);}}else{node.setAttributeNS(null,"r",style.pointRadius);}
if(typeof style.rotation!="undefined"&&pos){var rotation=OpenLayers.String.format("rotate(${0} ${1} ${2})",[style.rotation,pos.x,pos.y]);node.setAttributeNS(null,"transform",rotation);}}
if(options.isFilled){node.setAttributeNS(null,"fill",style.fillColor);node.setAttributeNS(null,"fill-opacity",style.fillOpacity);}else{node.setAttributeNS(null,"fill","none");}
if(options.isStroked){node.setAttributeNS(null,"stroke",style.strokeColor);node.setAttributeNS(null,"stroke-opacity",style.strokeOpacity);node.setAttributeNS(null,"stroke-width",style.strokeWidth*widthFactor);node.setAttributeNS(null,"stroke-linecap",style.strokeLinecap);node.setAttributeNS(null,"stroke-linejoin","round");node.setAttributeNS(null,"stroke-dasharray",this.dashStyle(style,widthFactor));}else{node.setAttributeNS(null,"stroke","none");}
if(style.pointerEvents){node.setAttributeNS(null,"pointer-events",style.pointerEvents);}
if(style.cursor!=null){node.setAttributeNS(null,"cursor",style.cursor);}
return node;},dashStyle:function(style,widthFactor){var w=style.strokeWidth*widthFactor;switch(style.strokeDashstyle){case'solid':return'none';case'dot':return[1,4*w].join();case'dash':return[4*w,4*w].join();case'dashdot':return[4*w,4*w,1,4*w].join();case'longdash':return[8*w,4*w].join();case'longdashdot':return[8*w,4*w,1,4*w].join();default:return style.strokeDashstyle.replace(/ /g,",");}},createNode:function(type,id){var node=document.createElementNS(this.xmlns,type);if(id){node.setAttributeNS(null,"id",id);}
return node;},nodeTypeCompare:function(node,type){return(type==node.nodeName);},createRenderRoot:function(){return this.nodeFactory(this.container.id+"_svgRoot","svg");},createRoot:function(suffix){return this.nodeFactory(this.container.id+suffix,"g");},createDefs:function(){var defs=this.nodeFactory(this.container.id+"_defs","defs");this.rendererRoot.appendChild(defs);return defs;},drawPoint:function(node,geometry){return this.drawCircle(node,geometry,1);},drawCircle:function(node,geometry,radius){var resolution=this.getResolution();var x=(geometry.x/resolution+this.left);var y=(this.top-geometry.y/resolution);if(this.inValidRange(x,y)){node.setAttributeNS(null,"cx",x);node.setAttributeNS(null,"cy",y);node.setAttributeNS(null,"r",radius);return node;}else{return false;}},drawLineString:function(node,geometry){var componentsResult=this.getComponentsString(geometry.components);if(componentsResult.path){node.setAttributeNS(null,"points",componentsResult.path);return(componentsResult.complete?node:null);}else{return false;}},drawLinearRing:function(node,geometry){var componentsResult=this.getComponentsString(geometry.components);if(componentsResult.path){node.setAttributeNS(null,"points",componentsResult.path);return(componentsResult.complete?node:null);}else{return false;}},drawPolygon:function(node,geometry){var d="";var draw=true;var complete=true;var linearRingResult,path;for(var j=0,len=geometry.components.length;j<len;j++){d+=" M";linearRingResult=this.getComponentsString(geometry.components[j].components," ");path=linearRingResult.path;if(path){d+=" "+path;complete=linearRingResult.complete&&complete;}else{draw=false;}}
d+=" z";if(draw){node.setAttributeNS(null,"d",d);node.setAttributeNS(null,"fill-rule","evenodd");return complete?node:null;}else{return false;}},drawRectangle:function(node,geometry){var resolution=this.getResolution();var x=(geometry.x/resolution+this.left);var y=(this.top-geometry.y/resolution);if(this.inValidRange(x,y)){node.setAttributeNS(null,"x",x);node.setAttributeNS(null,"y",y);node.setAttributeNS(null,"width",geometry.width/resolution);node.setAttributeNS(null,"height",geometry.height/resolution);return node;}else{return false;}},drawSurface:function(node,geometry){var d=null;var draw=true;for(var i=0,len=geometry.components.length;i<len;i++){if((i%3)==0&&(i/3)==0){var component=this.getShortString(geometry.components[i]);if(!component){draw=false;}
d="M "+component;}else if((i%3)==1){var component=this.getShortString(geometry.components[i]);if(!component){draw=false;}
d+=" C "+component;}else{var component=this.getShortString(geometry.components[i]);if(!component){draw=false;}
d+=" "+component;}}
d+=" Z";if(draw){node.setAttributeNS(null,"d",d);return node;}else{return false;}},drawText:function(featureId,style,location){var resolution=this.getResolution();var x=(location.x/resolution+this.left);var y=(location.y/resolution-this.top);var label=this.nodeFactory(featureId+this.LABEL_ID_SUFFIX,"text");var tspan=this.nodeFactory(featureId+this.LABEL_ID_SUFFIX+"_tspan","tspan");label.setAttributeNS(null,"x",x);label.setAttributeNS(null,"y",-y);label.setAttributeNS(null,"pointer-events","none");if(style.fontColor){label.setAttributeNS(null,"fill",style.fontColor);}
if(style.fontFamily){label.setAttributeNS(null,"font-family",style.fontFamily);}
if(style.fontSize){label.setAttributeNS(null,"font-size",style.fontSize);}
if(style.fontWeight){label.setAttributeNS(null,"font-weight",style.fontWeight);}
var align=style.labelAlign||"cm";label.setAttributeNS(null,"text-anchor",OpenLayers.Renderer.SVG.LABEL_ALIGN[align[0]]||"middle");if(this.isGecko){label.setAttributeNS(null,"dominant-baseline",OpenLayers.Renderer.SVG.LABEL_ALIGN[align[1]]||"central");}else{tspan.setAttributeNS(null,"baseline-shift",OpenLayers.Renderer.SVG.LABEL_VSHIFT[align[1]]||"-35%");}
tspan.textContent=style.label;if(!label.parentNode){label.appendChild(tspan);this.textRoot.appendChild(label);}},getComponentsString:function(components,separator){var renderCmp=[];var complete=true;var len=components.length;var strings=[];var str,component,j;for(var i=0;i<len;i++){component=components[i];renderCmp.push(component);str=this.getShortString(component);if(str){strings.push(str);}else{if(i>0){if(this.getShortString(components[i-1])){strings.push(this.clipLine(components[i],components[i-1]));}}
if(i<len-1){if(this.getShortString(components[i+1])){strings.push(this.clipLine(components[i],components[i+1]));}}
complete=false;}}
return{path:strings.join(separator||","),complete:complete};},clipLine:function(badComponent,goodComponent){if(goodComponent.equals(badComponent)){return"";}
var resolution=this.getResolution();var maxX=this.MAX_PIXEL-this.translationParameters.x;var maxY=this.MAX_PIXEL-this.translationParameters.y;var x1=goodComponent.x/resolution+this.left;var y1=this.top-goodComponent.y/resolution;var x2=badComponent.x/resolution+this.left;var y2=this.top-badComponent.y/resolution;var k;if(x2<-maxX||x2>maxX){k=(y2-y1)/(x2-x1);x2=x2<0?-maxX:maxX;y2=y1+(x2-x1)*k;}
if(y2<-maxY||y2>maxY){k=(x2-x1)/(y2-y1);y2=y2<0?-maxY:maxY;x2=x1+(y2-y1)*k;}
return x2+","+y2;},getShortString:function(point){var resolution=this.getResolution();var x=(point.x/resolution+this.left);var y=(this.top-point.y/resolution);if(this.inValidRange(x,y)){return x+","+y;}else{return false;}},getPosition:function(node){return({x:parseFloat(node.getAttributeNS(null,"cx")),y:parseFloat(node.getAttributeNS(null,"cy"))});},importSymbol:function(graphicName){if(!this.defs){this.defs=this.createDefs();}
var id=this.container.id+"-"+graphicName;if(document.getElementById(id)!=null){return id;}
var symbol=OpenLayers.Renderer.symbol[graphicName];if(!symbol){throw new Error(graphicName+' is not a valid symbol name');return;}
var symbolNode=this.nodeFactory(id,"symbol");var node=this.nodeFactory(null,"polygon");symbolNode.appendChild(node);var symbolExtent=new OpenLayers.Bounds(Number.MAX_VALUE,Number.MAX_VALUE,0,0);var points="";var x,y;for(var i=0;i<symbol.length;i=i+2){x=symbol[i];y=symbol[i+1];symbolExtent.left=Math.min(symbolExtent.left,x);symbolExtent.bottom=Math.min(symbolExtent.bottom,y);symbolExtent.right=Math.max(symbolExtent.right,x);symbolExtent.top=Math.max(symbolExtent.top,y);points+=" "+x+","+y;}
node.setAttributeNS(null,"points",points);var width=symbolExtent.getWidth();var height=symbolExtent.getHeight();var viewBox=[symbolExtent.left-width,symbolExtent.bottom-height,width*3,height*3];symbolNode.setAttributeNS(null,"viewBox",viewBox.join(" "));this.symbolSize[id]=Math.max(width,height)*3;this.defs.appendChild(symbolNode);return symbolNode.id;},CLASS_NAME:"OpenLayers.Renderer.SVG"});OpenLayers.Renderer.SVG.LABEL_ALIGN={"l":"start","r":"end","b":"bottom","t":"hanging"};OpenLayers.Renderer.SVG.LABEL_VSHIFT={"t":"-70%","b":"0"};