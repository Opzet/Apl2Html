(function ($) {
    $(window).on("load", function() {

      // SYMBOL POPUP
      // --------------
      $("a.class_def, a.function_def, a.var_def, a.parameter_def").on('click', function(e) {
        var symbolReferences = symbols[e.target.id];

        if (!symbolReferences)
          return;

        e.stopPropagation();

        var symLinks = '<h3>Usages of: '+e.target.id+'</h3><ul>';
        symbolReferences.forEach(function(item, index) {
          symLinks += '<li><a href=\"'+item+'\">'+item+'</a></li>';
        });
        symLinks += '</ul>';

        existingPopup = $('.alert');
        $('<div class="alert alert-success alert-dismissable">'+
            '<button type="button" class="close" ' + 
                    'data-dismiss="alert" aria-hidden="true">' + 
                '&times;' + 
            '</button>' + 
            symLinks +
         '</div>').prependTo("#symbolPopup");

        // Prevent events from getting pass .popup
        $(".alert").on("click", function(e) {
          e.stopPropagation();
        });

        hidePopup(existingPopup, 200);
      });

      $("body").on("click", function() {
        if (!$(".alert").is(":hidden")) {
          hidePopup($(".alert"), 0);
        }
      });

      function hidePopup(element, waitMillis) {
        setTimeout(function() {
          element.fadeOut(300, 'linear', function() {
            element.remove();
          });
        }, waitMillis);
      }

      // SLOW NAVIGATION
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

      $('body').append("<div id=\"symbolPopup\"></div>");
    });
})(jQuery);