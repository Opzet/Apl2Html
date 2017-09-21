(function ($) {
    $(window).on("load", function() {
      $("a.class_def, a.function_def, a.var_def, a.parameter_def").on('click', function(e) {
        var symbolReferences = symbols[e.target.id];

        if (!symbolReferences)
          return;

        var symLinks = '<h3>Found usages of: '+e.target.id+'</h3><ul>';
        symbolReferences.forEach(function(item, index) {
          symLinks += '<li><a href=\"'+item+'\">'+item+'</a></li>';
        });
        symLinks += '</ul>';

        $('<div class="alert alert-success alert-dismissable">'+
            '<button type="button" class="close" ' + 
                    'data-dismiss="alert" aria-hidden="true">' + 
                '&times;' + 
            '</button>' + 
            symLinks +
         '</div>').appendTo("#symbolPopup");
      });

      $("a").on('click', function(e) {
    
       // prevent default anchor click behavior
       e.preventDefault();
    
       // store hash
       var hash = this.hash;
    
       if (!hash)
        return;

       // animate
       $('html, body').animate({
           scrollTop: $(hash).offset().top
         }, 300, function(){
    
           // when done, add hash to url
           // (default click behaviour)
           window.location.hash = hash;
         });
      });

      $('body').append("<div style=\"position:fixed;bottom:0;left:0;width:100%;\" id=\"symbolPopup\"></div>");
    });
})(jQuery);